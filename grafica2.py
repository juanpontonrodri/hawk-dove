import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Define the matrix names with their values
matrix_names = {
    '-1,10,0,5': 'Original Matrix: -1, 10, 0, 5',
    '-5,8,2,6': 'Higher Penalty for Conflict (H, H): -5, 8, 2, 6',
    '-2,10,1,7': 'Incentive for Cooperation (D, D): -2, 10, 1, 7',
    '-3,9,2,6': 'Balanced Risk and Reward: -3, 9, 2, 6',
    '-4,7,3,8': 'High Reward for Dove Action: -4, 7, 3, 8',
    '-10,10,0,5': 'Conflict-Focused Matrix: -10, 10, 0, 5',
    '-2,7,2,9': 'Cooperation-Focused Matrix: -2, 7, 2, 9',
    '-3,6,1,7': 'Intermediate Matrix: -3, 6, 1, 7'
}

# Define the agent type mapping
agent_type_mapping = {
    'NN_Agent1': 'NN_Agent',
    'NN_Agent2': 'NN_Agent',
    'RLAgent1': 'RL_Agent',
    'RLAgent2': 'RL_Agent',
    'RandomAgent1': 'RandomAgent',
    'RandomAgent2': 'RandomAgent',
    'HAgent1': 'HAgent',
    'HAgent2': 'HAgent',
    'AlternatingAgent1': 'AlternatingAgent',
    'AlternatingAgent2': 'AlternatingAgent',
    'TFTAgent1': 'TFTAgent',
    'TFTAgent2': 'TFTAgent',
    'DAgent1': 'DAgent',
    'DAgent2': 'DAgent'
}

# Define colors for each agent type
agent_type_colors = {
    'NN_Agent': 'blue',
    'RL_Agent': 'green',
    'RandomAgent': 'red',
    'HAgent': 'black',
    'AlternatingAgent': 'orange',
    'TFTAgent': 'cyan',
    'DAgent': 'magenta'
}

def plot_scores_by_matrix_type(filename):
    # Read the CSV file
    df = pd.read_csv(filename)
    
    # Create a column 'Matrix' combining the values W, X, Y, Z
    df['Matrix'] = df.apply(lambda row: f'{row.Matrix_W},{row.Matrix_X},{row.Matrix_Y},{row.Matrix_Z}', axis=1)
    
    # Map the matrix values to their descriptive names
    df['Matrix Name'] = df['Matrix'].map(matrix_names)
    
    # Map agent names to their types
    df['Agent Type'] = df['Nombre del jugador'].map(agent_type_mapping)
    
    # Set the style of the plot
    sns.set(style="whitegrid")
    
    # Create a figure and an axis
    plt.figure(figsize=(20, 10))
    
    # Create a bar plot with 'Matrix Name' on the x-axis and 'Puntos' on the y-axis, colored by 'Agent Type'
    sns.barplot(data=df, x='Matrix Name', y='Puntos', hue='Agent Type', palette=agent_type_colors)
    
    # Add title and labels
    plt.title('Agent Scores by Matrix Type')
    plt.xlabel('Matrix Type')
    plt.ylabel('Points')
    
    # Rotate x-axis labels for better readability
    plt.xticks(rotation=45)
    
    # Move the legend outside the plot
    plt.legend(title='Agent Type', bbox_to_anchor=(1.05, 1), loc='upper left')
    
    # Adjust layout to avoid cutting off labels
    plt.tight_layout()
    
    # Show the plot
    plt.show()

# Define the filename containing the combined results
results_file = 'compiled_agent_scores.csv'

# Generate the plot of agent scores by matrix type
plot_scores_by_matrix_type(results_file)
