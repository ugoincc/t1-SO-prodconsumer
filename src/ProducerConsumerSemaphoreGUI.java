import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumerSemaphoreGUI {

    private static final int BUFFER_SIZE = 5;
    private final Queue<Integer> buffer = new LinkedList<>();

    private final Semaphore itemsSemaphore = new Semaphore(0);
    private final Semaphore spacesSemaphore = new Semaphore(BUFFER_SIZE);
    private final Lock lock = new ReentrantLock();

    private final JFrame frame;
    private final JPanel bufferPanel;
    private final JLabel producerStatus;
    private final JLabel consumerStatus;

    public ProducerConsumerSemaphoreGUI() {
        frame = new JFrame("Produtor-Consumidor com Semáforos");
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
                if (i < buffer.size()) {
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

                    spacesSemaphore.acquire(); // Espera espaço disponível no buffer
                    lock.lock(); // Trava o buffer para evitar concorrência
                    try {
                        buffer.add(item);
                        setProducerStatus("Colocou " + item + " no buffer");
                        updateBufferPanel();
                        item++;
                    } finally {
                        lock.unlock(); // Libera o buffer
                    }
                    itemsSemaphore.release(); // Sinaliza que há um item disponível

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
                    itemsSemaphore.acquire(); // Espera por um item disponível

                    lock.lock(); // Trava o buffer para evitar concorrência
                    int item;
                    try {
                        item = buffer.poll();
                        setConsumerStatus("Consumindo " + item);
                        updateBufferPanel();
                    } finally {
                        lock.unlock(); // Libera o buffer
                    }

                    spacesSemaphore.release(); // Sinaliza que há espaço disponível
                    Thread.sleep(1750); // Tempo de consumo
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        ProducerConsumerSemaphoreGUI app = new ProducerConsumerSemaphoreGUI();
        Producer producer = app.new Producer();
        Consumer consumer = app.new Consumer();

        producer.start();
        consumer.start();
    }
}
