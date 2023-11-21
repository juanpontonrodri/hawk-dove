
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class GUI extends JFrame implements ActionListener {
    JLabel leftPanelRoundsLabel;
    JLabel parametersLabel;
    JList<String> list;
    private MainAgent mainAgent;
    private JPanel rightPanel;
    private JTextArea rightPanelLoggingTextArea;
    private LoggingOutputStream loggingOutputStream;

    private JTable rankingTable;
    private DefaultTableModel tableModel;
    private JPanel centralBottomSubpanel;
            JPanel centralTopSubpanel = new JPanel(new BorderLayout());


    public GUI() {
        initUI();
    }

    public GUI (MainAgent agent) {
        mainAgent = agent;
        initUI();
        this.setTitle("Hawk-Dove tournament");

         try {
            URL iconURL = new URL("https://cdn-icons-png.flaticon.com/512/1198/1198083.png"); 
            ImageIcon icon = new ImageIcon(iconURL);
            this.setIconImage(icon.getImage());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        loggingOutputStream = new LoggingOutputStream (rightPanelLoggingTextArea);
    }

    public void log (String s) {
        Runnable appendLine = () -> {
            rightPanelLoggingTextArea.append('[' + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] - " + s);
            rightPanelLoggingTextArea.setCaretPosition(rightPanelLoggingTextArea.getDocument().getLength());
        };
        SwingUtilities.invokeLater(appendLine);
    }

    public OutputStream getLoggingOutputStream() {
        return loggingOutputStream;
    }

    public void logLine (String s) {
        log(s + "\n");
    }

    public void setPlayersUI (String[] players) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.clear();

        if(players.length == 0) {

            listModel.addElement("Empty");
        }else{
        for (String s : players) {
            listModel.addElement(s);
        }}
        list.setModel(listModel);
    }

    public void initUI() {
        setTitle("GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));
        setPreferredSize(new Dimension(1920, 1080));
        setJMenuBar(createMainMenuBar());
        setContentPane(createMainContentPane());
        pack();
        //setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private Container createMainContentPane() {
        JPanel pane = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.gridy = 0;
        gc.weightx = 0.5;
        gc.weighty = 0.5;

        //LEFT PANEL
        gc.gridx = 0;
        gc.weightx = 1;
        pane.add(createLeftPanel(), gc);

        //CENTRAL PANEL
        gc.gridx = 1;
        gc.weightx = 8;
        pane.add(createCentralPanel(), gc);

        //RIGHT PANEL
        gc.gridx = 2;
        gc.weightx = 8;
        pane.add(createRightPanel(), gc);
        return pane;
    }

    public JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        int totalGames = (mainAgent.getGameParameters().N * (mainAgent.getGameParameters().N - 1) * mainAgent.getGameParameters().R) / 2;
        leftPanelRoundsLabel = new JLabel("Round " + mainAgent.getRoundsPlayed() + "/" + totalGames);
        
        JButton leftPanelNewButton = new JButton("New");
        leftPanelNewButton.addActionListener(actionEvent -> mainAgent.newGame());
        JButton leftPanelStopButton = new JButton("Stop");
        leftPanelStopButton.addActionListener(this);
        JButton leftPanelContinueButton = new JButton("Continue");
        leftPanelContinueButton.addActionListener(this);
        JButton updatePlayersButton = new JButton("Update players");
        updatePlayersButton.addActionListener(actionEvent -> mainAgent.updatePlayers());

        parametersLabel = new JLabel("Players:" + mainAgent.getGameParameters().N + ", Rounds: " + mainAgent.getGameParameters().R + " Games: " + mainAgent.gamesPlayed);

        String[] columnNames = {"Puesto", "Name", "Points"};
        Object[][] playerData = mainAgent.getPlayerData();

        tableModel = new DefaultTableModel(playerData, columnNames);
        rankingTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);

                // Cambia el color de fondo de las filas alternas
                if (isRowSelected(row)) {
                    comp.setBackground(getSelectionBackground());
                } else {
                    if (row % 2 == 0) {
                        comp.setBackground(new Color(240, 240, 240)); // Color de fondo para filas pares
                    } else {
                        comp.setBackground(getBackground()); // Color de fondo para filas impares
                    }
                }
                return comp;
            }
        };

        // Crea el JScrollPane con la tabla
        JScrollPane tableScrollPane = new JScrollPane(rankingTable);

        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.gridx = 0;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        gc.gridy = 0;
        leftPanel.add(leftPanelRoundsLabel, gc);
        gc.gridy = 1;
        leftPanel.add(leftPanelNewButton, gc);
        gc.gridy = 2;
        leftPanel.add(leftPanelStopButton, gc);     
        // Agregar espacio entre la etiqueta y la tabla utilizando Insets
        gc.gridy = 3;
        leftPanel.add(leftPanelContinueButton, gc);
        gc.gridy = 4;
        leftPanel.add(updatePlayersButton, gc);
        gc.gridy = 5;
        leftPanel.add(parametersLabel, gc);
        gc.gridy = 6;
        gc.weighty = 1.0;
        leftPanel.add(tableScrollPane, gc);

        return leftPanel;
    }

    public void updateTableData() {
        Object[][] playerData = mainAgent.getPlayerData();
        tableModel.setDataVector(playerData, new String[]{"Puesto", "Name", "Points"});
        tableModel.fireTableDataChanged();
    }

    public void updateParametersLabel() {
        // Actualizar el texto con la cantidad actualizada de jugadores y rondas
        parametersLabel.setText("Players:" + mainAgent.getGameParameters().N + ", Rounds: " + mainAgent.getGameParameters().R + " Games: " + mainAgent.gamesPlayed);
        int totalGames = (mainAgent.getGameParameters().N * (mainAgent.getGameParameters().N - 1) * mainAgent.getGameParameters().R) / 2;
        leftPanelRoundsLabel.setText("Round " + mainAgent.getRoundsPlayed() + "/" + totalGames);

    }

    private JPanel createCentralPanel() {
        JPanel centralPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 0.5;

        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.gridx = 0;

        gc.gridy = 0;
        gc.weighty = 1;
        centralPanel.add(createCentralTopSubpanel(), gc);
        gc.gridy = 1;
        gc.weighty = 4;
        initializeBottomSubpanel();
        centralPanel.add(centralBottomSubpanel, gc);

        return centralPanel;
    }

    private JPanel createCentralTopSubpanel() {
    
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement("Empty");
        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.setVisibleRowCount(5);
        JScrollPane listScrollPane = new JScrollPane(list);
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedPlayer = (String) list.getSelectedValue();
                if (selectedPlayer != null) {
                    updateTable(selectedPlayer);
    
                    // Llamar a updatePlayerInfo para mostrar la información del jugador seleccionado
                    updatePlayerInfo(selectedPlayer);
                }
            }
        });
    
        
    
        JPanel playerInfoPanel = new JPanel(new BorderLayout());
        JLabel info1 = new JLabel("Selected player info");
        playerInfoPanel.add(info1, BorderLayout.NORTH);
    
        JPanel infoPanel = new JPanel(new GridLayout(6, 1));
        playerInfoPanel.add(infoPanel, BorderLayout.CENTER);
    
        centralTopSubpanel.add(listScrollPane, BorderLayout.WEST);
        centralTopSubpanel.add(playerInfoPanel, BorderLayout.EAST);
    
        return centralTopSubpanel;
    }
    
    // Método para actualizar la información del jugador seleccionado
    private void updatePlayerInfo(String playerName) {
        int[] stats = mainAgent.getstats(playerName);

        JPanel playerInfoPanel = ((JPanel) ((BorderLayout) centralTopSubpanel.getLayout()).getLayoutComponent(BorderLayout.EAST));
        JPanel infoPanel = (JPanel) playerInfoPanel.getComponent(1);
    
        infoPanel.removeAll();
    

        if (stats != null) {
            JLabel winsLabel = new JLabel("Games Won: " + stats[0]);
            JLabel lossesLabel = new JLabel("Games Lost: " + stats[1]);
            JLabel drawsLabel = new JLabel("Games Draw: " + stats[2]);
            JLabel roundWinJLabel = new JLabel("Round Won: " + stats[3]);
            JLabel roundLostJLabel = new JLabel("Round Lost: " + stats[4]);
            JLabel roundDrawJLabel = new JLabel("Round Draw: " + stats[5]);

            // Calculando estadísticas adicionales
            int totalGames = stats[0] + stats[1] + stats[2];
            double winPercentage = (double) stats[0] / totalGames * 100;
            double lossPercentage = (double) stats[1] / totalGames * 100;
            double drawPercentage = (double) stats[2] / totalGames * 100;

            double avgScorePerGame = (double) mainAgent.getPlayerPoints(playerName) / totalGames;

            double roundWinPercentage = (double) stats[3] / (stats[3] + stats[4] + stats[5]) * 100;
            double roundLossPercentage = (double) stats[4] / (stats[3] + stats[4] + stats[5]) * 100;
            double roundDrawPercentage = (double) stats[5] / (stats[3] + stats[4] + stats[5]) * 100;

            JLabel winPercentageLabel = new JLabel("Win Percentage: " + String.format("%.2f", winPercentage) + "%");
            JLabel lossPercentageLabel = new JLabel("Loss Percentage: " + String.format("%.2f", lossPercentage) + "%");
            JLabel drawPercentageLabel = new JLabel("Draw Percentage: " + String.format("%.2f", drawPercentage) + "%");
            JLabel avgScoreLabel = new JLabel("Average Score per Game: " + String.format("%.2f", avgScorePerGame));
            JLabel roundWinPercentageLabel = new JLabel("Round Win Percentage: " + String.format("%.2f", roundWinPercentage) + "%");
            JLabel roundLossPercentageLabel = new JLabel("Round Loss Percentage: " + String.format("%.2f", roundLossPercentage) + "%");
            JLabel roundDrawPercentageLabel = new JLabel("Round Draw Percentage: " + String.format("%.2f", roundDrawPercentage) + "%");

            infoPanel.add(winsLabel);
            infoPanel.add(lossesLabel);
            infoPanel.add(drawsLabel);
            infoPanel.add(roundWinJLabel);
            infoPanel.add(roundLostJLabel);
            infoPanel.add(roundDrawJLabel);
            infoPanel.add(winPercentageLabel);
            infoPanel.add(lossPercentageLabel);
            infoPanel.add(drawPercentageLabel);
            infoPanel.add(roundWinPercentageLabel);
            infoPanel.add(roundLossPercentageLabel);
            infoPanel.add(roundDrawPercentageLabel);
            infoPanel.add(avgScoreLabel);
        } else {
            JLabel noStatsLabel = new JLabel("No stats available for " + playerName);
            infoPanel.add(noStatsLabel);
        }

        infoPanel.revalidate();
        infoPanel.repaint();
    }

    
    

    
    
    
    

    /* private JPanel createCentralBottomSubpanel() {

        Object[][] gameHistory = {
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"}
        };
        JPanel centralBottomSubpanel = new JPanel(new GridBagLayout());

        JLabel payoffLabel = new JLabel("Player Results");
        String[] columnNames = {"Order", "Player Points", "Opponent Points", "Winner"};

        


        
        historyTableModel= new DefaultTableModel(gameHistory, columnNames);
        histoyJTable = new JTable(historyTableModel);


        JScrollPane playerScrollPane = new JScrollPane(histoyJTable);

        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 0.5;
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weighty = 0.5;
        centralBottomSubpanel.add(payoffLabel, gc);
        gc.gridy = 1;
        gc.gridx = 0;
        gc.weighty = 2;
        centralBottomSubpanel.add(playerScrollPane, gc);

        return centralBottomSubpanel;
    } 

    private void setResultsData(String playerName) {
        Object[][] gameHistory = mainAgent.getGameHistory(playerName);
        
        historyTableModel.setDataVector(gameHistory, new String[]{"Order", "Player Points", "Opponent Points", "Winner"});
        historyTableModel.fireTableDataChanged();
        
    } */

    /* private JPanel createCentralBottomSubpanel() {
        JPanel centralBottomSubpanel = new JPanel(new GridBagLayout());

        Object[] nullPointerWorkAround = {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"};

        Object[][] data = {
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"},
                {"*", "*", "*", "*", "*", "*", "*", "*", "*", "*"}
        };

        JLabel payoffLabel = new JLabel("Player Results");
        JTable payoffTable = new JTable(data, nullPointerWorkAround);
        payoffTable.setTableHeader(null);
        payoffTable.setEnabled(false);
        
        JScrollPane player1ScrollPane = new JScrollPane(payoffTable);

        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 0.5;
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weighty = 0.5;
        centralBottomSubpanel.add(payoffLabel, gc);
        gc.gridy = 1;
        gc.gridx = 0;
        gc.weighty = 2;
        centralBottomSubpanel.add(player1ScrollPane, gc);

        return centralBottomSubpanel;
    }  */


    private JTable payoffTable;
    private JScrollPane player1ScrollPane;
    private DefaultTableModel tableModel2;
    private String[] columnNames = {"Results", "Results", "Results", "Results", "Results", "Results"};
    
   private void initializeBottomSubpanel() {
    centralBottomSubpanel = new JPanel(new GridBagLayout());

    Object[][] initialData = getEmptyTableData(); // Método para obtener datos iniciales vacíos
    tableModel2 = new DefaultTableModel(initialData, columnNames);
    payoffTable = new JTable(tableModel2);
    payoffTable.setTableHeader(null);
    payoffTable.setEnabled(false);

    player1ScrollPane = new JScrollPane(payoffTable);

    GridBagConstraints gc = new GridBagConstraints();
    gc.weightx = 0.5;
    gc.fill = GridBagConstraints.BOTH;
    gc.anchor = GridBagConstraints.FIRST_LINE_START;

    gc.gridx = 0;
    gc.gridy = 0;
    gc.weighty = 0.5;
    centralBottomSubpanel.add(new JLabel("Player Results"), gc); // Etiqueta de resultados
    gc.gridy = 1;
    gc.weighty = 2;
    centralBottomSubpanel.add(player1ScrollPane, gc); // Tabla de resultados

    // Definir el diseño de la tabla para que muestre las celdas en filas
    payoffTable.setCellSelectionEnabled(true);
    payoffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    payoffTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    payoffTable.setRowHeight(40); // Altura de cada fila
    payoffTable.setColumnSelectionAllowed(false);
}

private void updateTable(String playerName) {
    Object[][] gameHistory = mainAgent.getGameHistory(playerName);

    if (gameHistory == null || gameHistory.length == 0) {
        // No hay datos disponibles para el jugador seleccionado
        tableModel2.setDataVector(getEmptyTableData(), columnNames);
        return;
    }

    Object[][] tableData = new Object[calculateRowCount(gameHistory)][columnNames.length];

    int rowIndex = 0;
    int cellIndex = 0;
    for (int i = 0; i < gameHistory.length; i++) {
        int gameOrder = (int) gameHistory[i][0];
        int playerPoints = (int) gameHistory[i][1];
        int opponentPoints = (int) gameHistory[i][2];
        String rival= (String) gameHistory[i][3];

        String gameResult = gameOrder + ". " + playerPoints + ":" + opponentPoints + ":" + rival;

        // Agregar el resultado del juego a la matriz de datos de la tabla
        tableData[rowIndex][cellIndex] = gameResult;

        rowIndex++;
        if (rowIndex >= tableData.length) {
            rowIndex = 0;
            cellIndex++;
        }
    }

    // Establecer los datos en la tabla
    tableModel2.setDataVector(tableData, columnNames);
    tableModel2.fireTableDataChanged();

    for (int i = 0; i < columnNames.length; i++) {
    TableColumn column = payoffTable.getColumnModel().getColumn(i);
    column.setPreferredWidth(100); // Cambia este valor según sea necesario para ajustar el ancho de las celdas
    
    }
    // Renderizador de celdas personalizado para cambiar el color de fondo
    TableCellRenderer renderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    
            if (value != null && value instanceof String) {
                String cellValue = (String) value;
    
                // Obtener los puntos del jugador y su oponente desde la celda
                String[] parts = cellValue.split("\\.");
                if (parts.length > 1) {
                    String[] points = parts[1].trim().split(":");
                    if (points.length > 1) {
                        int playerPoints = Integer.parseInt(points[0]);
                        int opponentPoints = Integer.parseInt(points[1]);
    
                        // Cambiar el color de fondo según el resultado
                        if (playerPoints > opponentPoints) {
                            comp.setBackground(Color.GREEN); // Gana el jugador
                        } else {
                            if (playerPoints < opponentPoints) {
                                                            comp.setBackground(Color.RED); // Pierde el jugador o empate

                            } else {
                                comp.setBackground(Color.YELLOW); // Empate
                            }
                        }
                    }
                }
            } else {
                // Valor nulo o no es una cadena, configurar un comportamiento predeterminado
                comp.setBackground(table.getBackground());
            }
    
            return comp;
        }
    };
    

    // Asignar el renderizador de celdas a todas las columnas
    for (int i = 0; i < tableModel2.getColumnCount(); i++) {
        payoffTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
    }
}

// Método para calcular la cantidad de filas necesarias en la tabla
private int calculateRowCount(Object[][] gameHistory) {
    int rows = gameHistory.length / columnNames.length;
    if (gameHistory.length % columnNames.length != 0) {
        rows++; // Agregar una fila si no se llena completamente la última fila
    }
    return rows;
}

private Object[][] getEmptyTableData() {
    Object[][] initialData = new Object[10][columnNames.length];
    for (int i = 0; i < initialData.length; i++) {
        for (int j = 0; j < initialData[i].length; j++) {
            initialData[i][j] = "*"; // Espacios en blanco
        }
    }
    return initialData;
}

    

    private JPanel createRightPanel() {
        rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weighty = 1d;
        c.weightx = 1d;

        rightPanelLoggingTextArea = new JTextArea("");
        rightPanelLoggingTextArea.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(rightPanelLoggingTextArea);
        rightPanel.add(jScrollPane, c);
        return rightPanel;
    }

    private JMenuBar createMainMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        

        JMenu menuEdit = new JMenu("Edit");
        JMenuItem resetPlayerEditMenu = new JMenuItem("Reset Players");
        resetPlayerEditMenu.setToolTipText("Reset all player");
        resetPlayerEditMenu.setActionCommand("reset_players");
        resetPlayerEditMenu.addActionListener(actionEvent -> mainAgent.resetPlayers());

        JMenuItem removePlayerEditMenu = new JMenuItem("Remove Player");
        removePlayerEditMenu.setToolTipText("Remove a player from the game");
        removePlayerEditMenu.setActionCommand("remove_player");
        removePlayerEditMenu.addActionListener(actionEvent -> {
            DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex != -1) {
                String selectedPlayer = model.getElementAt(selectedIndex);
                model.remove(selectedIndex); // Elimina el jugador de la lista visual
                mainAgent.removePlayerAgent(selectedPlayer);
            } else {
                JOptionPane.showMessageDialog(null, "Please select a player to remove.");
            }
        });
        menuEdit.add(removePlayerEditMenu);

        menuEdit.add(resetPlayerEditMenu);
        menuBar.add(menuEdit);

        JMenu menuRun = new JMenu("Run");

        JMenuItem newRunMenu = new JMenuItem("New");
        newRunMenu.setToolTipText("Starts a new series of games");
        newRunMenu.addActionListener(this);

        JMenuItem stopRunMenu = new JMenuItem("Stop");
        stopRunMenu.setToolTipText("Stops the execution of the current round");
        stopRunMenu.addActionListener(this);

        JMenuItem continueRunMenu = new JMenuItem("Continue");
        continueRunMenu.setToolTipText("Resume the execution");
        continueRunMenu.addActionListener(this);

        

        JMenuItem roundNumberRunMenu = new JMenuItem("Number of rounds");
        roundNumberRunMenu.setToolTipText("Change the number of rounds");
        roundNumberRunMenu.addActionListener(actionEvent -> {
            String userInput = JOptionPane.showInputDialog(new Frame("Configure rounds"), "How many rounds?");
            int rounds = Integer.parseInt(userInput);
            mainAgent.getGameParameters().setR(rounds);
            updateParametersLabel();
                
        });
        menuRun.add(newRunMenu);
        menuRun.add(stopRunMenu);
        menuRun.add(continueRunMenu);
        menuRun.add(roundNumberRunMenu);
        menuBar.add(menuRun);

        JMenu menuWindow = new JMenu("Window");

        JCheckBoxMenuItem toggleVerboseWindowMenu = new JCheckBoxMenuItem("Verbose", true);
        toggleVerboseWindowMenu.addActionListener(actionEvent -> rightPanel.setVisible(toggleVerboseWindowMenu.getState()));

        menuWindow.add(toggleVerboseWindowMenu);
        menuBar.add(menuWindow);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Author: Juan Ponton Rodriguez", "About", JOptionPane.INFORMATION_MESSAGE);
        }); 
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        return menuBar;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton) {
            JButton button = (JButton) e.getSource();
            logLine("Button " + button.getText());
        } else if (e.getSource() instanceof JMenuItem) {
            JMenuItem menuItem = (JMenuItem) e.getSource();
            logLine("Menu " + menuItem.getText());
        }
    }

    public class LoggingOutputStream extends OutputStream {
        private JTextArea textArea;

        public LoggingOutputStream(JTextArea jTextArea) {
            textArea = jTextArea;
        }

        @Override
        public void write(int i) throws IOException {
            textArea.append(String.valueOf((char) i));
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
}
