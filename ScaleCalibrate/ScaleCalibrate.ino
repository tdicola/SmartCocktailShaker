// Smart Cocktail Shaker
// Scale Calibration Sketch

// Created by Tony DiCola (tony@tonydicola.com)
// Released under an MIT license (http://opensource.org/licenses/MIT).

// Configuration:
#define ADC_PIN            5              // Set this to the analog input which is connected to the instrument amp output.

#define ADC_SAMPLES        5              // Number of ADC readings to average for a sample.  This helps reduce noise from the ADC.

#define FILTER_SAMPLES     3              // Number of samples to use in moving average of samples.  This is a simple low pass
                                          // filter that helps reduce noise from the bridge & amplifier.

#define SAMPLE_PERIOD_MS   250            // How long to wait between measurements.

const float GRAMS_PER_MEASUREMENT = 1.0;  // Grams per measurement used to converts the measurement values into weight.
                                          // Set this by noting the filtered measurement for a known weight.
                                          // For example if a 200 gram weight has a filtered measurement value of 89.9 you
                                          // would set this to 200.0 / 89.9

// Internal state used by the sketch.
float filterSamples[FILTER_SAMPLES];
int filterIndex = 0;

void setup(void) 
{
  Serial.begin(9600);

  // Use internal 1.1V analog reference voltage.
  analogReference(INTERNAL);
  
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
  // Take a reading and filter it to generate a sample.
  float adc = readADC(ADC_PIN);
  float sample = filterSample(adc);
  // Print out the raw reading, filtered value, and weight.
  Serial.print("ADC: ");
  Serial.print(adc);
  Serial.print("\tFILTERED: ");
  Serial.print(sample);
  Serial.print("\tGRAMS: ");
  Serial.println(sample * GRAMS_PER_MEASUREMENT);
  // Wait until it's time for the next sample.
  delay(SAMPLE_PERIOD_MS);
}