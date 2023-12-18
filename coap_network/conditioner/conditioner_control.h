#ifndef CONDITIONER_CONTROL_H
#define CONDITIONER_CONTROL_H

#include "mode.h"  

void set_conditioner_state(int new_temperature, int new_fanSpeed, int new_humidity, Mode new_mode);

void set_conditioner_switch(bool new_state);

#endif /* CONDITIONER_CONTROL_H */
