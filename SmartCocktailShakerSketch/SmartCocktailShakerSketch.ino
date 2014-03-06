// Smart Cocktail Shaker
// Main Sketch

// Created by Tony DiCola (tony@tonydicola.com)
// Released under an MIT license (http://opensource.org/licenses/MIT).

// Configuration:
#define ADC_PIN                5    // Set this to the analog input which is connected to the instrument amp output.

#define ADC_SAMPLES            5    // Number of ADC readings to average for a sample.  This helps reduce noise from the ADC.

#define FILTER_SAMPLES         3    // Number of samples to use in moving average of samples.  This is a simple low pass
                                    // filter that helps reduce noise from the bridge & amplifier.

#define SAMPLE_PERIOD_MS       250  // How long to wait between measurements.

#define ZERO_OFFSET            0.0  // Zero offset value found from the calibration sketch.

#define GRAMS_PER_MEASUREMENT  0.0  // Grams per measurement value found from the calibration sketch.

// Internal state used by the sketch.
int filterSamples[FILTER_SAMPLES];
int filterIndex = 0;
unsigned long lastMeasure = 0;
float sample = 0;

void setup(void) 
{
  Serial.begin(9600);
  
  analogReference(EXTERNAL);
  
  // Initialize previous samples to 0.
  for (int i = 0; i < FILTER_SAMPLES; ++i) {
    filterSamples[i] = 0.0;
  }
}

// Query the analog pin for a value.
// Takes a number of ADC readings and averages them to reduce noise.
float readADC(int pin) {
  float total = 0.0;
  for (int i = 0; i < ADC_SAMPLES; ++i) {
    total += analogRead(pin);
  }
  return total / (float) ADC_SAMPLES;  
}

// Apply simple low pass filter by computing a moving average of samples.
float filterSample(float newSample) {
  // Store the sample in the current index and increment the index appropriately.
  filterSamples[filterIndex] = newSample;
  filterIndex += 1;
  filterIndex = filterIndex < FILTER_SAMPLES ? filterIndex : 0;
  // Sum all the sample values.
  float total = 0;
  for (int i = 0; i < FILTER_SAMPLES; ++i) {
     total += filterSamples[i];
  }
  // Return the average value of the sample values.
  return total / (float) FILTER_SAMPLES;
}

void loop(void) 
{
  // Check for a request to read the sample from the serial port.
  if (Serial.available() > 0) {
    char command = Serial.read();
    if (command == '?') {
      // Respond with last sample weight when '?' character is received.
      Serial.println(sample * GRAMS_PER_MEASUREMENT);
    }
  }
  
  // Update the sample every sample period.
  unsigned long time = millis();
  if (time - lastMeasure >= SAMPLE_PERIOD_MS) {
    sample = filterSample(readADC(ADC_PIN)) - ZERO_OFFSET;
    sample = sample < 0.0 ? 0.0 : sample;
    lastMeasure = time;
  }
  
  // Loop continuously without delay so requests on the serial port are responsive.
}
