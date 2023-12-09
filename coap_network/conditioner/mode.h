#ifndef MODE_H
#define MODE_H

// Definizione dell'enum per i modi di funzionamento del condizionatore
typedef enum {
    NONE = 0,
    HEATER = 1,
    HEATER_HUMIDIFIER = 2,
    HUMIDIFIER = 3,
    WIND = 4
} Mode;

#endif // MODE_H
