#ifndef CONDITIONER_CONTROL_H
#define CONDITIONER_CONTROL_H

#include "mode.h"  // Assicurati di includere tutti i file necessari per le dichiarazioni di tipo

void set_conditioner_state(int new_temperature, int new_fanSpeed, int new_humidity, Mode new_mode);

void set_conditioner_switch(bool new_state);

#endif /* CONDITIONER_CONTROL_H */
