/*
 * Copyright (c) 2020, Carlo Vallati, University of Pisa
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*---------------------------------------------------------------------------*/
#include "contiki.h"
#include "net/routing/routing.h"
#include "mqtt.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "lib/sensors.h"

#include "os/dev/leds.h"
#include "os/sys/log.h"
#include "mqtt-client.h"
#include "sys/node-id.h"

#include <string.h>
#include <strings.h>
/*---------------------------------------------------------------------------*/
#define LOG_MODULE "mqtt-client"
#ifdef MQTT_CLIENT_CONF_LOG_LEVEL
#define LOG_LEVEL MQTT_CLIENT_CONF_LOG_LEVEL
#else
#define LOG_LEVEL LOG_LEVEL_DBG
#endif

/*---------------------------------------------------------------------------*/
/* MQTT broker address. */
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"

static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Defaukt config values
#define DEFAULT_BROKER_PORT 1883

// We assume that the broker does not require authentication

/*---------------------------------------------------------------------------*/
/* Various states */
static uint8_t state;

#define STATE_INIT 0
#define STATE_NET_OK 1
#define STATE_CONNECTING 2
#define STATE_CONNECTED 3
#define STATE_SUBSCRIBED 4
#define STATE_DISCONNECTED 5

/*---------------------------------------------------------------------------*/
PROCESS_NAME(mqtt_client_process);
AUTOSTART_PROCESSES(&mqtt_client_process);

/*---------------------------------------------------------------------------*/
/* Maximum TCP segment size for outgoing segments of our socket */
#define MAX_TCP_SEGMENT_SIZE 32
#define CONFIG_IP_ADDR_STR_LEN 64
/*---------------------------------------------------------------------------*/
/*
 * Buffers for Client ID and Topics.
 * Make sure they are large enough to hold the entire respective string
 */
#define BUFFER_SIZE 64

static char client_id[BUFFER_SIZE];
static char pub_topic[BUFFER_SIZE];
static char sub_topic_humidity[BUFFER_SIZE];
static char sub_topic_temperature[BUFFER_SIZE];

// Periodic timer to check the state of the MQTT client
static struct etimer periodic_timer;

/*---------------------------------------------------------------------------*/
/*
 * The main MQTT buffers.
 * We will need to increase if we start publishing more data.
 */
#define APP_BUFFER_SIZE 512
static char app_buffer[APP_BUFFER_SIZE];
/*---------------------------------------------------------------------------*/
static struct mqtt_message *msg_ptr = 0;
static struct mqtt_connection conn;

mqtt_status_t status;
char broker_address[CONFIG_IP_ADDR_STR_LEN];

static int temperature;
static int humidity;
static int target_temperature = -1;
static int target_humidity = -1;
static char *start_msg = NULL;
static int temp_increment = 0;
static int hum_increment = 0;
static bool heating = false;
static bool humidifying = false;

/*---------------------------------------------------------------------------*/
PROCESS(mqtt_client_process, "MQTT Client");

/*---------------------------------------------------------------------------*/
static void
pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk,
            uint16_t chunk_len)
{
  LOG_INFO("Pub Handler: topic='%s' (len=%u), chunk_len=%u\n", topic,
         topic_len, chunk_len);

  if (strcmp(topic, "temperature_condition") == 0)
  {
    printf("Received temperature command\n");
    printf("%s\n", chunk);
    // search in chunk (formatted as json) the value of temperature
    start_msg = strstr((char *)chunk, "\"temperature\":");
    if (start_msg)
    {
      start_msg += strlen("\"temperature\":");
      target_temperature = atoi(start_msg);
    }
    // start_msg = strstr((char *)chunk, "\"increment\":");
    if (start_msg)
    {
      start_msg += strlen("\"increment\":");
      temp_increment = atoi(start_msg);
    }
    if (temperature < target_temperature)
    {
      heating = true;
    }
    else
    {
      heating = false;
    }
    return;
  }
  if (strcmp(topic, "humidity_condition") == 0)
  {
    printf("Received humidity command\n");
    printf("%s\n", chunk);
    // search in chunk (formatted as json) the value of humidity
    start_msg = strstr((char *)chunk, "\"humidity\":");
    if (start_msg)
    {
      start_msg += strlen("\"humidity\":");
      target_humidity = atoi(start_msg);
      printf("Humidity: %d\n", target_humidity);
    }
    // start_msg = strstr((char *)chunk, "\"increment\":");
    if (start_msg)
    {
      start_msg += strlen("\"increment\":");
      hum_increment = atoi(start_msg);
    }
    if (humidity < target_humidity)
    {
      humidifying = true;
    }
    else
    {
      humidifying = false;
    }
    return;
  }
}
/*---------------------------------------------------------------------------*/
static void
mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data)
{
  switch (event)
  {
  case MQTT_EVENT_CONNECTED:
  {
    printf("Application has a MQTT connection\n");

    state = STATE_CONNECTED;
    break;
  }
  case MQTT_EVENT_DISCONNECTED:
  {
    printf("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));

    state = STATE_DISCONNECTED;
    process_poll(&mqtt_client_process);
    break;
  }
  case MQTT_EVENT_PUBLISH:
  {
    msg_ptr = data;

    pub_handler(msg_ptr->topic, strlen(msg_ptr->topic),
                msg_ptr->payload_chunk, msg_ptr->payload_length);
    break;
  }
  case MQTT_EVENT_SUBACK:
  {
#if MQTT_311
    mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;

    if (suback_event->success)
    {
      printf("Application is subscribed to topic successfully\n");
    }
    else
    {
      printf("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
    }
#else
    printf("Application is subscribed to topic successfully\n");
#endif
    break;
  }
  case MQTT_EVENT_UNSUBACK:
  {
    printf("Application is unsubscribed to topic successfully\n");
    break;
  }
  case MQTT_EVENT_PUBACK:
  {
    printf("Publishing complete.\n");
    break;
  }
  default:
    printf("Application got a unhandled MQTT event: %i\n", event);
    break;
  }
}

static bool
have_connectivity(void)
{
  if (uip_ds6_get_global(ADDR_PREFERRED) == NULL ||
      uip_ds6_defrt_choose() == NULL)
  {
    return false;
  }
  return true;
}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(mqtt_client_process, ev, data)
{

  PROCESS_BEGIN();

  printf("MQTT Client Process\n");

  humidity = 5 + (rand() % 90);
  temperature = (rand() % 45);

  // Initialize the ClientID as MAC address
  snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
           linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
           linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
           linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

  // Broker registration
  mqtt_register(&conn, &mqtt_client_process, client_id, mqtt_event,
                MAX_TCP_SEGMENT_SIZE);

  state = STATE_INIT;

  // Initialize periodic timer to check the status
  etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);

  /* Main loop */
  while (1)
  {

    PROCESS_YIELD();

    if ((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) ||
        ev == PROCESS_EVENT_POLL)
    {

      if (state == STATE_INIT)
      {
        if (have_connectivity() == true)
          state = STATE_NET_OK;
        leds_off(LEDS_ALL);
      }

      if (state == STATE_NET_OK)
      {
        // Connect to MQTT server
        printf("Connecting!\n");

        memcpy(broker_address, broker_ip, strlen(broker_ip));

        mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,
                     (DEFAULT_PUBLISH_INTERVAL * 3) / CLOCK_SECOND,
                     MQTT_CLEAN_SESSION_ON);
        state = STATE_CONNECTING;
      }

      if (state == STATE_CONNECTED)
      {

        // Subscribe to the "humidity_condition" topic
        strcpy(sub_topic_humidity, "humidity_condition");
        mqtt_subscribe(&conn, NULL, sub_topic_humidity, MQTT_QOS_LEVEL_0);

        // Check the subscription status for "humidity_condition"
        printf("Subscribing to 'humidity_condition' topic!\n");
        if (status == MQTT_STATUS_OUT_QUEUE_FULL)
        {
          LOG_ERR("Tried to subscribe to 'humidity_condition' but command queue was full!\n");
          PROCESS_EXIT();
        }

        // Subscribe to the "temperature_condition" topic
        strcpy(sub_topic_temperature, "temperature_condition");
        mqtt_subscribe(&conn, NULL, sub_topic_temperature, MQTT_QOS_LEVEL_0);

        // Check the subscription status for "temperature_condition"
        printf("Subscribing to 'temperature_condition' topic!\n");
        if (status == MQTT_STATUS_OUT_QUEUE_FULL)
        {
          LOG_ERR("Tried to subscribe to 'temperature_condition' but command queue was full!\n");
          PROCESS_EXIT();
        }
        leds_on(LEDS_GREEN);
        leds_off(LEDS_BLUE);
        state = STATE_SUBSCRIBED;
        PUBLISH_INTERVAL = (10 * CLOCK_SECOND);
        STATE_MACHINE_PERIODIC = PUBLISH_INTERVAL;
      }

      if (state == STATE_SUBSCRIBED)
      {
        LOG_INFO("I try to publish a message\n");
        // Publish something
        sprintf(pub_topic, "%s", "temperature_humidity");

        // humidity
        if (target_humidity == -1)
        {
          // Applica l'incremento al valore corrente di humidity [-3,+3]
          humidity += (rand() % 7 - 3);
        }
        else
        {
          // start increasing/decreasing values humidity until it reaches the target
          if (humidifying)
          {
            humidity += hum_increment;
            if (humidity >= target_humidity)
            {
              target_humidity = -1;
              hum_increment = 0;
            }
          }
          else
          {
            humidity -= hum_increment;
            if (humidity <= target_humidity)
            {
              target_humidity = -1;
              hum_increment = 0;
            }
          }
        }
        // Assicurati che humidity sia compreso tra 0 e 100
        if (humidity < 0)
        {
          humidity = 0;
        }
        else if (humidity > 100)
        {
          humidity = 100;
        }

        // temperature
        if (target_temperature == -1)
        {
          // Applica l'incremento al valore corrente di temperature [-1,+1]
          temperature += (rand() % 2 - 1);
        }
        else
        {
          // start increasing/decreasing values temperature until it reaches the target
          if (heating)
          {
            temperature += temp_increment;
            if (temperature >= target_temperature)
            {
              target_temperature = -1;
              temp_increment = 0;
            }
          }
          else
          {
            temperature -= temp_increment;
            if (temperature <= target_temperature)
            {
              target_temperature = -1;
              temp_increment = 0;
            }
          }
        }
        sprintf(app_buffer, "{\"nodeId\": %d, \"temperature\": %d,\"humidity\": %d}", node_id, temperature, humidity);

        mqtt_publish(&conn, NULL, pub_topic, (uint8_t *)app_buffer,
                     strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
        leds_off(LEDS_ALL);
        leds_on(LEDS_GREEN);
      }

      else if (state == STATE_DISCONNECTED)
      {
        LOG_ERR("Disconnected form MQTT broker\n");
        // Recover from error
        state = STATE_INIT;
        leds_off(LEDS_ALL);
        leds_on(LEDS_RED);
      }

      etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
    }
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
