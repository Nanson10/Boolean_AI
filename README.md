# Boolean AI - Neural Network Simulator

A Java-based neural network simulator that uses boolean logic to simulate neuron activation patterns. The application can learn to generate sequences through reward/punishment-based training.

## Features

- **Boolean Matrix Neural Network**: Visualize neural activation patterns in real-time
- **Manual Mode**: Interactive control with manual reward/punishment
- **Auto Mode**: Automated learning to generate the alphabet sequence (A-Z)
- **Real-time Visualization**: See neural network state changes as they happen
- **Progress Tracking**: Monitor learning progress in Auto Mode

## Building the Project

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Build Commands

```bash
# Clean and compile
mvn clean compile

# Build executable JAR
mvn clean package
```

The executable JAR will be created at: `target/boolean_ai-1.0-SNAPSHOT.jar`

## Running the Application

### Option 1: Run the JAR (Recommended)
```bash
java -jar target/boolean_ai-1.0-SNAPSHOT.jar
```

This will launch a GUI dialog where you can choose between:
- **Manual Mode**: Interactive neural network with manual controls
- **Auto Mode**: Automated learning to generate alphabet sequence

### Option 2: Run with Maven
```bash
# Launch the Driver (selection dialog)
mvn exec:java -Dexec.mainClass="nanson.Driver"

# Or run Manual Mode directly
mvn exec:java -Dexec.mainClass="nanson.BooleanMatrixDisplay"

# Or run Auto Mode directly
mvn exec:java -Dexec.mainClass="nanson.AutoGrader"
```

## How It Works

### Neural Network Architecture
- Each neuron has configurable incoming connections with boolean weights
- Activation is determined by weighted sum of inputs vs. threshold
- The network adjusts its behavior through selective rewiring of poorly-performing neurons

### Manual Mode
1. Click **Run Cycle** to execute a learning iteration
2. View the character output generated from neuron states
3. Click **Reward** if the output is correct (strengthens current neural pathways)
4. Click **Punish** if incorrect (rewires poorly-performing neurons)

### Auto Mode
- Automatically attempts to learn the alphabet sequence (A, B, C, ... Z)
- Rewards correct characters, punishes incorrect ones
- Tracks current progress and furthest progress achieved
- Continuously cycles until stopped

## UI Components

- **Neuron Matrix**: Visual representation of neural activation states (green = active, white = inactive)
- **Iteration Counter**: Shows current/total iterations per cycle
- **Activation Percentage**: Percentage of neurons currently active
- **Threshold Multiplier**: Current activation threshold
- **Character Display**: Current character generated from output neurons
- **Progress Tracker** (Auto Mode only): Current and furthest learning progress

## Configuration

Default configuration in `Constants.java`:
- Matrix Size: 20x20 neurons
- Neurons per cycle: 1000
- Incoming connections per neuron: 5
- Result length: 7 bits

## Project Structure

```
src/main/java/nanson/
├── Driver.java              # Main entry point with mode selection
├── Simulator.java           # Core neural network simulation
├── BooleanMatrixDisplay.java # GUI visualization
├── AutoGrader.java          # Automated learning mode
├── Constants.java           # Configuration constants
└── Utilities.java           # Helper functions
```

## License

This project is provided as-is for educational purposes.
