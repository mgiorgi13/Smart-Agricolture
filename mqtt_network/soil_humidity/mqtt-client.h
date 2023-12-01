
/*---------------------------------------------------------------------------*/
#ifndef MQTT_CLIENT_H_
#define MQTT_CLIENT_H_
/*---------------------------------------------------------------------------*/
#include <string.h>

#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)
#define DEFAULT_STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static long PUBLISH_INTERVAL = DEFAULT_PUBLISH_INTERVAL;
static long STATE_MACHINE_PERIODIC = DEFAULT_STATE_MACHINE_PERIODIC;

#endif /* MQTT_CLIENT_H_ */
/*---------------------------------------------------------------------------*/
