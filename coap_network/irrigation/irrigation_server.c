// /*
//  * Copyright (c) 2020, Carlo Vallati, University of Pisa
//  * All rights reserved.
//  *
//  * Redistribution and use in source and binary forms, with or without
//  * modification, are permitted provided that the following conditions
//  * are met:
//  * 1. Redistributions of source code must retain the above copyright
//  *    notice, this list of conditions and the following disclaimer.
//  * 2. Redistributions in binary form must reproduce the above copyright
//  *    notice, this list of conditions and the following disclaimer in the
//  *    documentation and/or other materials provided with the distribution.
//  * 3. Neither the name of the Institute nor the names of its contributors
//  *    may be used to endorse or promote products derived from this software
//  *    without specific prior written permission.
//  *
//  * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
//  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
//  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
//  * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
//  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
//  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
//  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
//  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
//  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
//  * SUCH DAMAGE.
//  *
//  * This file is part of the Contiki operating system.
//  */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki.h"
#include "sys/etimer.h"

#include "node-id.h"
#include "net/ipv6/simple-udp.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "net/ipv6/uip-debug.h"
#include "routing/routing.h"
#include "irrigation_control.h"
#include "os/dev/button-hal.h"
#include "os/dev/leds.h"

#include "coap-engine.h"
#include "coap-blocking-api.h"

#define SERVER_EP "coap://[fd00::1]:5683"
#define CONN_TRY_INTERVAL 1
#define REG_TRY_INTERVAL 1
#define SIMULATION_INTERVAL 8
#define SENSOR_TYPE "irrigation_switch"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

PROCESS(irrigation_server, "Server for the Irrigation actuator");
AUTOSTART_PROCESSES(&irrigation_server);

//*************************** GLOBAL VARIABLES *****************************//
char *service_url = "/registration";

static bool connected = false;
static bool registered = false;

static struct etimer wait_connectivity;
static struct etimer wait_registration;
static bool blink_state = false;
static bool activate = false;
// static struct etimer simulation;

extern coap_resource_t irrigation_actuator;
extern coap_resource_t irrigation_switch;

//*************************** UTILITY FUNCTIONS *****************************//
static void check_connection()
{
    if (!NETSTACK_ROUTING.node_is_reachable())
    {
        LOG_INFO("BR not reachable\n");
        etimer_reset(&wait_connectivity);
    }
    else
    {
        LOG_INFO("BR reachable");
        // TODO: notificare in qualche modo che si è connessi
        // gli altri hanno usato i led
        connected = true;
    }
}

void client_chunk_handler(coap_message_t *response)
{
    const uint8_t *chunk;

    if (response == NULL)
    {
        LOG_INFO("Request timed out\n");
        etimer_set(&wait_registration, CLOCK_SECOND * REG_TRY_INTERVAL);
        return;
    }

    int len = coap_get_payload(response, &chunk);

    if (strncmp((char *)chunk, "Success", len) == 0)
        registered = true;
    else
        etimer_set(&wait_registration, CLOCK_SECOND * REG_TRY_INTERVAL);
}

//*************************** THREAD *****************************//
PROCESS_THREAD(irrigation_server, ev, data)
{
    PROCESS_BEGIN();

    static coap_endpoint_t server_ep;
    static coap_message_t request[1]; // This way the packet can be treated as pointer as usual

    etimer_set(&wait_connectivity, CLOCK_SECOND * CONN_TRY_INTERVAL);

    // try to connect to BR router
    while (!connected)
    {
        if (blink_state)
        {
            leds_off(LEDS_RED);
        }
        else
        {
            leds_on(LEDS_RED);
        }

        blink_state = !blink_state;
        PROCESS_WAIT_UNTIL(etimer_expired(&wait_connectivity));
        check_connection();
    }
    LOG_INFO("CONNECTED\n");
    leds_on(LEDS_RED);

    // try to register to the coap server
    while (!registered)
    {
        LOG_INFO("Sending registration message\n");
        if (blink_state)
        {
            leds_off(LEDS_GREEN);
        }
        else
        {
            leds_on(LEDS_GREEN);
        }

        blink_state = !blink_state;

        coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

        coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
        coap_set_header_uri_path(request, service_url);
        coap_set_payload(request, (uint8_t *)SENSOR_TYPE, sizeof(SENSOR_TYPE) - 1);
        COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);

        // wait for the timer to expire
        PROCESS_WAIT_UNTIL(etimer_expired(&wait_registration));
    }
    LOG_INFO("REGISTERED\nStarting irrigation server");
    leds_off(LEDS_ALL);
    leds_on(LEDS_GREEN);

    // RESOURCES ACTIVATION
    coap_activate_resource(&irrigation_switch, "irrigation_switch");

    while (1)
    {
        PROCESS_WAIT_EVENT();
        if (ev == button_hal_press_event)
        {
            activate = !activate;
            set_irrigation_switch(activate);
            if (activate)
            {
                leds_off(LEDS_ALL);
                leds_on(LEDS_BLUE);
            }
            else
                leds_off(LEDS_BLUE);
        }
    }

    PROCESS_END();
}