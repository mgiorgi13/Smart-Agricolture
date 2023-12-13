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
static int isActive = 0;

/**************** REST: Temperature **********************/
static void get_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
// static void status_event_handler(void);

EVENT_RESOURCE(irrigation_switch,
               "</irrigation_switch>;title=\"Irrigation Switch\";",
               get_switch_handler,
               NULL,
               put_switch_handler,
               NULL,
               NULL);

static void get_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling status get request...\n");

    coap_set_header_content_format(response, APPLICATION_JSON);
    // set status according to the value of isActive
    if (isActive)
        sprintf((char *)buffer, "{\"switch\": \"on\"}");
    else
        sprintf((char *)buffer, "{\"switch\": \"off\"}");
    coap_set_payload(response, buffer, strlen((char *)buffer));
}

static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    const char *payload = (char *)request->payload;
    LOG_INFO("%s",payload);

    char *switch_start = strstr(payload, "\"switch\":");
    
    if (!switch_start) {
        coap_set_status_code(response, BAD_REQUEST_4_00);
        return;
    }

    switch_start += strlen("\"switch\":");

    // Skip any whitespace after the colon
    while (*switch_start == ' ') {
        switch_start++;
    }

    // Check the value of the switch
    if (strncmp(switch_start, "\"on\"", 4) == 0) {
        isActive = 1;
    } else if (strncmp(switch_start, "\"off\"", 5) == 0) {
        isActive = 0;
    } else {
        coap_set_status_code(response, BAD_REQUEST_4_00);
        return;
    }
}