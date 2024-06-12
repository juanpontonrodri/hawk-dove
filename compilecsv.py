import os
import pandas as pd

def extract_params_from_filename(filename):
    # Extracts V, W, X, Y, Z from filename format 'results_V_W_X_Y_Z.csv'
    parts = filename.split('_')
    V = parts[1]
    W = parts[2]
    X = parts[3]
    Y = parts[4]
    Z = parts[5].replace('.csv', '')
    return V, W, X, Y, Z

def compile_agent_scores(directory):
    # Initialize an empty dataframe to hold all results
    all_results = pd.DataFrame()

    # Iterate over all files in the directory
    for filename in os.listdir(directory):
        if filename.startswith('results_') and filename.endswith('.csv'):
            V, W, X, Y, Z = extract_params_from_filename(filename)
            filepath = os.path.join(directory, filename)
            
            # Read the CSV file into a DataFrame
            df = pd.read_csv(filepath, usecols=['Nombre del jugador', 'Puntos'])
            
            # Add columns for the parameters
            df['Rounds'] = V
            df['Matrix_W'] = W
            df['Matrix_X'] = X
            df['Matrix_Y'] = Y
            df['Matrix_Z'] = Z
            
            # Append the DataFrame to the cumulative results
            all_results = pd.concat([all_results, df], ignore_index=True)
    
    # Save the combined results to a new CSV file
    all_results.to_csv(os.path.join(directory, 'compiled_agent_scores.csv'), index=False)

# Define the directory containing the results CSV files
results_directory = './'

# Compile agent scores from all CSV files in the directory
compile_agent_scores(results_directory)
