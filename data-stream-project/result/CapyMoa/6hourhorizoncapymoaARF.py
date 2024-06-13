import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from capymoa.stream import stream_from_file, Schema
from capymoa.evaluation import prequential_evaluation
from capymoa.regressor import AdaptiveRandomForestRegressor, ARFFIMTDD

# Read the dataset using pandas to inspect
df = pd.read_csv("river_radar/preprocessNormalized.csv", header=None)

# Extract the column names and create Schema
attribute_names = df.columns[:-1].tolist()  # All columns except the target attribute

# Create schema using from_custom method with enforce_regression=True
schema = Schema.from_custom(
    feature_names=[f"attrib_{i}" for i in range(len(attribute_names))],
    enforce_regression=True
)

# Save the preprocessed data to a temporary CSV file for streaming
preprocessed_file = "river_radar/preprocessed_temp.csv"
df.to_csv(preprocessed_file, index=False, header=False)

# Initialise the stream correctly
stream = stream_from_file(path_to_csv_or_arff=preprocessed_file, enforce_regression=True)

# Verify the schema inferred from the stream
# print(stream.get_schema())

# Initialise the regressor and run the evaluation
# Define the base tree learner
tree_learner = ARFFIMTDD(
    schema=schema,
    subspace_size_size=60,
    grace_period=200,
    split_confidence=0.1
)

# Use a MOA wrapper if drift detection method wanted
arf_regressor = AdaptiveRandomForestRegressor(
    schema=schema,
    ensemble_size=25, 
    max_features=0.6,  
    lambda_param=6.0,
    drift_detection_method=None,
    warning_detection_method=None, # wave movement made this worse to have
    disable_drift_detection=True,
    disable_background_learner=False,
    tree_learner=tree_learner
)

# Evaluate using prequential_evaluation
results_arf = prequential_evaluation(
    stream=stream,
    learner=arf_regressor,
    window_size=4500,
    store_predictions=True,
    store_y=True
)

# Extract ground truth and predictions
ground_truth_y = np.array(results_arf['ground_truth_y']) # was for testing
predictions = np.array(results_arf['predictions'])

# Calculate and print RMSE for the entire dataset
# rmse = np.sqrt(np.mean((ground_truth_y - predictions) ** 2))
# print(f'Overall RMSE: {rmse}')

# # Step 7: Calculate the predicted water level by adding the initial water level to each prediction
initial_water_level = df[df.columns[-2]].values  # Initial water level for the first instance

# # Adjust the predictions by adding the current water level to each prediction
predictions += initial_water_level[:len(predictions)]

# ground truth test
ground_truth_y += initial_water_level[:len(predictions)] # for testing

# Define the flood level
flood_level = 4

# Plot the actual vs predicted water levels
plt.figure(figsize=(60, 5))
plt.plot(initial_water_level, label='Actual Water Level', color='green')
plt.plot(predictions, label='Predicted Water Level', color='red', linestyle='dashed')
plt.axhline(y=flood_level, color='purple', linestyle='-', label='Flood Level')
plt.xlabel('Instance')
plt.ylabel('Water Level')
plt.title('Predicted vs. Actual Water Level')
plt.legend()
plt.grid(True)
plt.show()


# Plot the actual vs predicted water levels for instances 56000 to 57000 for presentation
start, end = 56000, 57000
plt.figure(figsize=(20, 10))
plt.plot(range(start, end), initial_water_level[start:end], label='Actual Water Level', color='green')
plt.plot(range(start, end), predictions[start:end], label='Predicted Water Level', color='red')
plt.axhline(y=flood_level, color='purple', linestyle='-', label='Flood Level')
plt.xlabel('Instance')
plt.ylabel('Water Level')
plt.title(f'Predicted vs. Actual Water Level (Instances {start} to {end})')
plt.legend()
plt.grid(True)
plt.show()