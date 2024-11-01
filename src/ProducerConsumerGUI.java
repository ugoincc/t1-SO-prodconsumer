import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProducerConsumerGUI {

    private static final int BUFFER_SIZE = 5;
    private static final BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(BUFFER_SIZE);

    private final JFrame frame;
    private final JPanel bufferPanel;
    private final JLabel producerStatus;
    private final JLabel consumerStatus;

    public ProducerConsumerGUI() {
        frame = new JFrame("Produtor-Consumidor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        bufferPanel = new JPanel();
        bufferPanel.setLayout(new GridLayout(1, BUFFER_SIZE, 5, 5));
        for (int i = 0; i < BUFFER_SIZE; i++) {
            JLabel label = new JLabel("Vazio", SwingConstants.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            bufferPanel.add(label);
        }

        producerStatus = new JLabel("Produtor: Aguardando...", SwingConstants.CENTER);
        consumerStatus = new JLabel("Consumidor: Aguardando...", SwingConstants.CENTER);

        frame.add(bufferPanel, BorderLayout.CENTER);
        frame.add(producerStatus, BorderLayout.NORTH);
        frame.add(consumerStatus, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    public void updateBufferPanel() {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < BUFFER_SIZE; i++) {
                JLabel label = (JLabel) bufferPanel.getComponent(i);
                if (i < queue.size()) {
                    label.setText("Item");
                    label.setBackground(Color.GREEN);
                    label.setOpaque(true);
                } else {
                    label.setText("Vazio");
                    label.setBackground(null);
                    label.setOpaque(false);
                }
            }
        });
    }

    public void setProducerStatus(String status) {
        SwingUtilities.invokeLater(() -> producerStatus.setText("Produtor: " + status));
    }

    public void setConsumerStatus(String status) {
        SwingUtilities.invokeLater(() -> consumerStatus.setText("Consumidor: " + status));
    }

    class Producer extends Thread {
        @Override
        public void run() {
            int item = 0;
            try {
                while (true) {
                    setProducerStatus("Produzindo " + item);
                    Thread.sleep(500);

                    queue.put(item);  // Bloqueia se a fila estiver cheia
                    setProducerStatus("Colocou " + item + " no buffer");
                    updateBufferPanel();
                    item++;

                    Thread.sleep(500); // Tempo entre produções
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    class Consumer extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    setConsumerStatus("Aguardando item");
                    int item = queue.take();  // Bloqueia se a fila estiver vazia
                    setConsumerStatus("Consumindo " + item);
                    updateBufferPanel();

                    Thread.sleep(1750); // Tempo de consumo
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        ProducerConsumerGUI app = new ProducerConsumerGUI();
        Producer producer = app.new Producer();
        Consumer consumer = app.new Consumer();

        producer.start();
        consumer.start();
    }
}
