#include <stdlib.h>
#include <time.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

/* Log configuration */
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

/**************** RESOURCES **********************/
// #include "global_variables.h"

#define VARIATION 1

static int temperature = 24;

/**************** REST: Temperature **********************/
static void get_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void put_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
// static void temperature_event_handler(void);

EVENT_RESOURCE(heater_actuator,
               "</heater_actuator>;title=\"Heater Actuator\";",
               get_temperature_handler,
               NULL,
               put_temperature_handler,
               NULL,
               NULL);

static void get_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  LOG_INFO("Handling temperature get request...\n");
  // char *msg;

  coap_set_header_content_format(response, APPLICATION_JSON);
  sprintf((char *)buffer, "{\"temperature\": %d}", temperature);
  coap_set_payload(response, buffer, strlen((char *)buffer));

  // static const size_t max_char_len = 4; //-dd\0
  // msg = (char *)malloc((max_char_len) * sizeof(char));
  // snprintf(msg, max_char_len, "%d", temperature);

  // // prepare buffer
  // size_t len = strlen(msg);
  // memcpy(buffer, (const void *)msg, len);
  // free(msg);

  // // COAP FUNCTIONS
  // coap_set_header_content_format(response, TEXT_PLAIN);
  // coap_set_header_etag(response, (uint8_t *)&len, 1);
  // coap_set_payload(response, buffer, len);
}

static void put_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  size_t len = 0;
  const char *text = NULL;
  char temp[3];
  memset(temp, 0, 2);

  bool success = false;

  len = coap_get_post_variable(request, "temperature", &text); // dd\0
  if (len > 1 && len < 4)
  {
    memcpy(temp, text, len);
    temperature = atoi(temp);
    // if (strncmp(mode, "ON", len) == 0)
    // {
    //   ventilation_on = true;
    //   leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
    //   LOG_INFO("Ventilation System ON\n");
    // }
    // else if (strncmp(mode, "OFF", len) == 0)
    // {
    //   ventilation_on = false;
    //   leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
    //   LOG_INFO("Ventilation System OFF\n");
    // }
    success = true;
  }

  if (!success)
  {
    coap_set_status_code(response, BAD_REQUEST_4_00);
  }
}

// static void temperature_event_handler(void)
// {
//   LOG_INFO("Event_Handler\n");
//   if (!isActive)
//   {
//     return; // DOES NOTHING SINCE IT IS TURNED OFF
//   }

//   // extimate new temperature
//   srand(time(NULL));
//   int new_temp = temperature;
//   int random = rand() % 8; // generate 0, 1, 2, 3, 4, 5, 6, 7

//   if (random < 2)
//   {                  // 25% of changing the value
//     if (random == 0) // decrease
//       new_temp -= VARIATION;
//     else // increase
//       new_temp += VARIATION;
//   }

//   // if not equal
//   if (new_temp != temperature)
//   {
//     temperature = new_temp;
//     coap_notify_observers(&heater_actuator);
//   }
// }