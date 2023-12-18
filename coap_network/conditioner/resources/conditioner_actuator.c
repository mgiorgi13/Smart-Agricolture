#include <stdlib.h>
#include <time.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"
#include "conditioner_control.h"

/* Log configuration */
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

/**************** RESOURCES **********************/
static int temperature = 0;
static int fanSpeed = 0;
static int humidity = 0;
static Mode mode = 0; // possible mode : 0:none, 1:heater, 2:heater_humidifer, 3:humidifier, 4:wind

/**************** REST: Temperature **********************/
static void get_status_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void put_status_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

EVENT_RESOURCE(conditioner_actuator,
               "</conditioner_actuator>;title=\"Conditioner Actuator\";",
               get_status_handler,
               NULL,
               put_status_handler,
               NULL,
               NULL);

static void get_status_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling status get request...\n");

    coap_set_header_content_format(response, APPLICATION_JSON);

    sprintf((char *)buffer,
            "{\"temperature\": %d, \"fanSpeed\": %d, \"humidity\": %d, \"mode\": %d}",
            temperature, fanSpeed, humidity, (int)mode);

    coap_set_payload(response, buffer, strlen((char *)buffer));
}

static void put_status_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    const char *payload = (char *)request->payload;

    char *temperature_start = strstr(payload, "\"temperature\":");
    char *fanSpeed_start = strstr(payload, "\"fanSpeed\":");
    char *humidity_start = strstr(payload, "\"humidity\":");
    char *mode_start = strstr(payload, "\"mode\":");

    if (!temperature_start || !fanSpeed_start || !humidity_start || !mode_start)
    {
        coap_set_status_code(response, BAD_REQUEST_4_00);
        return;
    }

    temperature_start += strlen("\"temperature\":");
    fanSpeed_start += strlen("\"fanSpeed\":");
    humidity_start += strlen("\"humidity\":");
    mode_start += strlen("\"mode\":");

    temperature = atoi(temperature_start);
    fanSpeed = atoi(fanSpeed_start);
    humidity = atoi(humidity_start);
    mode = atoi(mode_start);
}

#pragma GCC diagnostic ignored "-Wunused-function"
void set_conditioner_state(int temp, int fan, int hum, Mode m)
{
    temperature = temp;
    fanSpeed = fan;
    humidity = hum;
    mode = m;
}
#pragma GCC diagnostic warning "-Wunused-function"
