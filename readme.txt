# README

## Información del autor

- Nombre: Juan Ponton Rodriguez
- ID: 26

## Compilation and execution steps
- javac -cp jade.jar agents/*.java *.java
- java -cp .;jade.jar jade.Boot -notmp -gui -agents "MainAgent:MainAgent;Alice:agents.RandomAgent;Bob:agents.RandomAgent;Charlie:agents.RandomAgent;David:agents.RandomAgent;Eve:agents.RandomAgent;Frank:agents.RandomAgent;Grace:agents.RandomAgent;Hannah:agents.RandomAgent;Ian:agents.RandomAgent;"

- 50 agents:  java -cp .:jade.jar jade.Boot -notmp -gui -agents "MainAgent:MainAgent;Alice:agents.RandomAgent;Bob:agents.RandomAgent;Charlie:agents.RandomAgent;David:agents.RandomAgent;Eve:agents.RandomAgent;Frank:agents.RandomAgent;Grace:agents.RandomAgent;Hannah:agents.RandomAgent;Ian:agents.RandomAgent;Agent11:agents.RandomAgent;Agent12:agents.RandomAgent;Agent13:agents.RandomAgent;Agent14:agents.RandomAgent;Agent15:agents.RandomAgent;Agent16:agents.RandomAgent;Agent17:agents.RandomAgent;Agent18:agents.RandomAgent;Agent19:agents.RandomAgent;Agent20:agents.RandomAgent;Agent21:agents.RandomAgent;Agent22:agents.RandomAgent;Agent23:agents.RandomAgent;Agent24:agents.RandomAgent;Agent25:agents.RandomAgent;Agent26:agents.RandomAgent;Agent27:agents.RandomAgent;Agent28:agents.RandomAgent;Agent29:agents.RandomAgent;Agent30:agents.RandomAgent;Agent31:agents.RandomAgent;Agent32:agents.RandomAgent;Agent33:agents.RandomAgent;Agent34:agents.RandomAgent;Agent35:agents.RandomAgent;Agent36:agents.RandomAgent;Agent37:agents.RandomAgent;Agent38:agents.RandomAgent;Agent39:agents.RandomAgent;Agent40:agents.RandomAgent;Agent41:agents.RandomAgent;Agent42:agents.RandomAgent;Agent43:agents.RandomAgent;Agent44:agents.RandomAgent;Agent45:agents.RandomAgent;Agent46:agents.RandomAgent;Agent47:agents.RandomAgent;Agent48:agents.RandomAgent;Agent49:agents.RandomAgent;Agent50:agents.RandomAgent;"

## Extra features of MainAgent and GUI
- Ranking table updated every round
- Statistics info to check best agent
- History board to check graphycally the evolution of the agent

## RandomAgent Description
-It justs selects a random action from the list of possible actions

## RL_Agent Description

-It uses a very simple Q-Learning algorithm to learn the best action to take in each state. It uses a table to store the Q-Values for each state-action pair.
-It has three parameters: learning rate, discount factor and exploration rate. They were adjusted by trial and error against different agents strategies like Random Agents, Alternating Agents, TitForTat, Deterministic Agents, etc.

## NN_Agent Description
The NN_Agent employs a combination of a Self-Organizing Map (SOM) and Q-learning.
Class QLearningNeuron:
    decideAction(String state):
        - Receives the game state as a string (e.g., "HDHHD...").
        - Chooses an action (exploration or exploitation).
        - Returns a random action ("H" or "D") if no prior information is available.
    updateQTable(String state, String action, String nextState, double reward):
        - Updates Q-values based on the results of actions.
        - Uses the Q-learning formula to adjust the Q-table.

Class SelfOrganizingMap:
    determineNextMove(double[] state):
        - Converts the state vector into a string of 'H' and 'D' characters.
        - Determines the next move using the current BMU and Q-learning.
    createDHString(double[] state):
        - Transforms an array of state values into a string representing the opponent's moves.
    findBestBMU():
        - Identifies the Best Matching Unit (BMU) on the SOM grid.
    updateQTable(double[] state, String action, double[] nextState, double reward):
        - Converts state arrays into strings and updates the Q-table for the current BMU's neuron.

Class NN_Agent:
    PlayBehaviour (inner class):
        action():
            - Manages game states and transitions.
            - State ROUND:
                - For the first 10 rounds, randomly selects between "H" and "D".
                - After 10 rounds, uses SOM and Q-learning for action decisions.
                - Resets the round counter at the game's end.


## RL_NN_Agent Description
The same as before.

java -cp .;jade.jar jade.Boot -notmp -gui -agents "MainAgent:MainAgent;RL_Agent:agents.RL_Agent;Alice:agents.RandomAgent"
java -cp .;jade.jar jade.Boot -notmp -gui -agents "MainAgent:MainAgent;NN_Agent:agents.NN_Agent;Random:agents.RandomAgent"

java -cp .;jade.jar jade.Boot -notmp -gui -agents "MainAgent:MainAgent;RLAgent1:agents.RL_Agent;RLAgent2:agents.RL_Agent;PSI_26:agents.PSI_26;RandomAgent1:agents.RandomAgent;RandomAgent2:agents.RandomAgent;RandomAgent3:agents.RandomAgent;RandomAgent4:agents.RandomAgent;HAgent1:agents.HAgent;HAgent2:agents.HAgent;HAgent3:agents.HAgent;AlternatingAgent1:agents.AlternatingAgent;AlternatingAgent2:agents.AlternatingAgent;TFTAgent1:agents.TFT;TFTAgent2:agents.TFT"

java -cp .;jade.jar jade.Boot -notmp -gui -agents "MainAgent:MainAgent;RLAgent1:agents.RL_Agent;RLAgent2:agents.RL_Agent;RandomAgent1:agents.RandomAgent;RandomAgent2:agents.RandomAgent;HAgent1:agents.HAgent;HAgent2:agents.HAgent;AlternatingAgent1:agents.AlternatingAgent;AlternatingAgent2:agents.AlternatingAgent;TFTAgent1:agents.TFT;TFTAgent2:agents.TFT;DAgent1:agents.DAgent;DAgent2:agents.DAgent;NN_Agent1:agents.NN_Agent;NN_Agent2:agents.NN_Agent"

sin hagents:

java -cp .;jade.jar jade.Boot -notmp -gui -agents "MainAgent:MainAgent;RLAgent1:agents.RL_Agent;RLAgent2:agents.RL_Agent;RandomAgent1:agents.RandomAgent;RandomAgent2:agents.RandomAgent;AlternatingAgent1:agents.AlternatingAgent;AlternatingAgent2:agents.AlternatingAgent;TFTAgent1:agents.TFT;TFTAgent2:agents.TFT;DAgent1:agents.DAgent;DAgent2:agents.DAgent;NN_Agent1:agents.NN_Agent;NN_Agent2:agents.NN_Agent"
