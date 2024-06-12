import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Define the matrix names with their values
matrix_names = {
    '-1,10,0,5': 'Original Matrix: -1,10,0,5',
    '-5,8,2,6': 'Higher Penalty for Conflict (H, H): -5,8,2,6',
    '-2,10,1,7': 'Incentive for Cooperation (D, D): -2,10,1,7',
    '-3,9,2,6': 'Balanced Risk and Reward: -3,9,2,6',
    '-4,7,3,8': 'High Reward for Dove Action: -4,7,3,8',
    '-10,10,0,5': 'Conflict-Focused Matrix: -10,10,0,5',
    '-2,7,2,9': 'Cooperation-Focused Matrix: -2,7,2,9',
    '-3,6,1,7': 'Intermediate Matrix: -3,6,1,7'
}

def plot_agent_scores(filename):
    # Read the CSV file
    df = pd.read_csv(filename)
    
    # Create a column 'Matrix' combining the values W, X, Y, Z
    df['Matrix'] = df.apply(lambda row: f'{row.Matrix_W},{row.Matrix_X},{row.Matrix_Y},{row.Matrix_Z}', axis=1)
    
    # Map the matrix values to their names
    df['Matrix Name'] = df['Matrix'].map(matrix_names)
    
    # Set the style of the plot
    sns.set(style="whitegrid")
    
    # Create a figure and an axis
    plt.figure(figsize=(15, 10))
    
    # Create a bar plot
    sns.barplot(data=df, x='Nombre del jugador', y='Puntos', hue='Matrix Name')
    
    # Add title and labels
    plt.title('Agent Scores by Matrix')
    plt.xlabel('Agent')
    plt.ylabel('Points')
    
    # Rotate x-axis labels for better readability
    plt.xticks(rotation=45)
    
    # Move the legend outside the plot
    plt.legend(title='Matrix', bbox_to_anchor=(1.05, 1), loc='upper left')
    
    # Adjust layout to avoid cutting off labels
    plt.tight_layout()
    
    # Show the plot
    plt.show()

# Define the filename containing the combined results
results_file = 'compiled_agent_scores.csv'

# Generate the plot of agent scores
plot_agent_scores(results_file)
