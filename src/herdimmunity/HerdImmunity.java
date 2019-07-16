
package herdimmunity;

// imports
import java.io.*;
import java.util.Scanner;
import java.awt.*; //needed for graphics
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.*; //needed for graphics
import static javax.swing.JFrame.EXIT_ON_CLOSE; //needed for graphics

public class HerdImmunity extends JFrame {

    //FIELDS
    int numGenerations = 1000;
    int currGeneration = 1;
    
    //default values - can be changed
    int brn = 4; // Basic reproduction number - any number from 0 to 7
    double percentImmune = 50; // percentage of population that's immune 
    
    Color healthyColor = Color.LIGHT_GRAY;
    Color contagiousColor = Color.RED;
    Color dormantColor = Color.GRAY;
    Color immuneColor = Color.GREEN;


    int width = 800; //width of the window in pixels
    int height = 800;
    int borderWidth = 50;

    int numCellsX = 50; //width of the grid (in cells)
    int numCellsY = 50;

    int state[][] = new int[numCellsX][numCellsY]; // 0 - immune, 1 = healthy, 2 - contagious, 3 - infected but dormant
    int stateNext[][] = new int[numCellsX][numCellsY]; 
    
    int cellWidth = (width - 2*borderWidth) / numCellsX;
    int labelX = width / 2;
    int labelY = borderWidth;
    
    public void setParameters(){
        Scanner s = new Scanner(System.in);
        System.out.println("Enter the basic reproduction number (0-8) : ");
        brn = s.nextInt();
        System.out.println("Enter the population immunity (0-100): ");
        percentImmune = s.nextInt();
    }
            
    
    public void plantFirstGeneration() throws IOException {
        makeEveryoneHealthy();
        
        //plantVirusFromPoint(2, 4);
        plantVirusRandomly(5);
        
        plantImmunity();
        
        setInitialStateNext();
    }
    
    public void plantImmunity(){ // set immune cells based off percentImmune
        Random r = new Random();
        
        for (int i = 0; i < numCellsX; i++) {
            for (int j = 0; j < numCellsY; j++) {
                
                if (state[i][j] != 2){ // if the cell does not have the virus
                    int immunePicker = r.nextInt(100);
                    if (immunePicker < percentImmune){ 
                        state[i][j] = 0;
                    }
                
                }
            }
        }
    }
    
    public void setInitialStateNext(){ // so I don't have to code around the empty array values for stateNext later
        for (int i = 0; i < numCellsX; i++) {
            for (int j = 0; j < numCellsY; j++) {
                stateNext[i][j] = state[i][j];
            }
        }
    }
    
    //Sets all cells to healthy
    public void makeEveryoneHealthy() {
        for (int i = 0; i < numCellsX; i++) {
            for (int j = 0; j < numCellsY; j++) {
                state[i][j] = 1;
            }
        }
    }

    
    //reads the first generations' infected cells from a file
    public void plantVirusFromFile(String fileName) throws IOException {

        FileReader f = new FileReader(fileName);
        Scanner s = new Scanner(f);

        int x, y;

        while ( s.hasNext() ) {
            x = s.nextInt();
            y = s.nextInt();
            
            state[x][y] = 2;
        }
    }
    
    public void plantVirusFromPoint(int x, int y) { // plants a virus from a specified point
        state[x][y] = 2;
    }
    
    public void plantVirusRandomly(int numPoints) { // randomly plants the specified number of viruse points
        Random r = new Random();
        
        for (int i = 0; i <numPoints; i++) {
                int x = r.nextInt(numCellsX);
                int y = r.nextInt(numCellsY);
                state[x][y] = 2;
        } 
    }

   public void computeNextGeneration() {
        for (int i = 0; i < numCellsX; i++){
            for (int j = 0; j < numCellsY; j++){
                if (state[i][j] == 2){ // if the cell has the disease, infect other cells
                    infectNeighbors(i, j);
                    stateNext[i][j] = 3;
                }
            }
        }
    }

    //Overwrites the current generation's 2-D array with the values from the next generation's 2-D array
    public void plantNextGeneration() {
        currGeneration++;
        for (int i = 0; i < state.length; i++){
            for (int j = 0; j < state[i].length; j++){
                state[i][j] = stateNext[i][j];
            }
        }
    }

    
    //Infects neighbors of cell (i, j)
    public void infectNeighbors(int i, int j) {
        int numToInfect = brn;
        
        // currX and currY are the x and y of the cell being examined
        for (int a = -1; a < 2; a++) {
            int currX = i + a;

            for (int b = -1; b < 2; b++) {
                int currY = j + b;
                
                if (!checkOutOfRange(currX, currY)) { // if the cell is within range
                    
                    if  (numToInfect == 0){ // if we've infected all the cells we need to
                        break;
                    } 
                    
                    if (state[currX][currY] == 1 && stateNext[currX][currY] == 1) { // do not infect the cell if it is already infected, about to be infected, or immune
                        stateNext[currX][currY] = 2; // infect the cell
                        numToInfect--;
                    }
                }
            }
        }
    }
    
   public boolean checkOutOfRange( int i, int j ){ // checks for out-of-range index values of currX and currY
       if (i >= 0 && i < state.length && j >= 0 && j < state[i].length){
            return false;
        }
       return true;
   }

    
    //Makes the pause between generations
    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } 
        catch (Exception e) {}
    }

    
    //Displays the statistics at the top of the screen
    void drawLabel(Graphics g, int state) {
        g.setColor(Color.black);
        g.fillRect(0, 0, width, borderWidth);
        g.setColor(Color.yellow);
        g.drawString("Generation: " + state, labelX, labelY);
    }
    

    
    //Draws the current generation of living cells on the screen
    public void paint( Graphics g){
        Image img = createImage();
        g.drawImage(img,8,30,this);
    }
    
    //Draws the current generation of living cells on the screen
    public Image createImage(){
        BufferedImage bufferedImage = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();

        int x, y, i, j;
        
        x = borderWidth;
        y = borderWidth;

        drawLabel(g, currGeneration);
        
        g.setColor(Color.black);
        g.drawRect(0, 0, width, height);
        
        
        
        for (i = 0; i < numCellsX; i++) {
            for (j = 0; j < numCellsY; j++) {
          
                if (state[i][j] == 0){
                    g.setColor(immuneColor);
                    
                } else if (state[i][j] == 1){
                    g.setColor(healthyColor);
                    
                } else if (state[i][j] == 2){
                    g.setColor(contagiousColor);
                } else{
                    g.setColor(dormantColor);
                }
                
                g.fillRect(x, y, cellWidth, cellWidth);
                g.setColor(Color.black);
                g.drawRect(x, y, cellWidth, cellWidth);
                
                x += cellWidth;
            }
            x = borderWidth;
            y += cellWidth;
            
            //Fill this in
        }
        return bufferedImage;
    }
   


    //Sets up the JFrame screen
    public void initializeWindow() {
        setTitle("Herd Immunity Simulator");
        setSize(height, width);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(Color.black);
        setVisible(true); //calls paint() for the first time
    }
    
    
    //Main algorithm
    public static void main(String args[]) throws IOException {

        HerdImmunity currGame = new HerdImmunity();
        
        currGame.setParameters();
        currGame.initializeWindow();
        currGame.plantFirstGeneration(); //Sets the initial generation of living cells, either by reading from a file or creating them algorithmically

        for (int i = 1; i <= currGame.numGenerations; i++) { 
           sleep(100);
           currGame.computeNextGeneration(); // fills aliveNext array
           currGame.plantNextGeneration(); // copies aliveNext into alive
           currGame.repaint();
        }
        
    } 
    
} //end of class