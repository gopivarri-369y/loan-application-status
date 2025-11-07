package firstJava;



import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoanStatusForm extends JFrame {

    public LoanStatusForm() {
        setTitle("Loan Application Status Form");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form fields
        JLabel loanIdLabel = new JLabel("Loan ID:");
        JTextField loanIdField = new JTextField("LN20251005");

        JLabel applicantIdLabel = new JLabel("Applicant ID:");
        JTextField applicantIdField = new JTextField("APP1005");

        JLabel statusLabel = new JLabel("Status:");

        String[] statuses = {
                "Received",
                "Under Review",
                "Approved",
                "Rejected",
                "Disbursed",
                "On Hold"
        };
        JComboBox<String> statusCombo = new JComboBox<>(statuses);

        JLabel updatedByLabel = new JLabel("Updated By:");
        JTextField updatedByField = new JTextField("CreditOfficer01");

        JLabel timestampLabel = new JLabel("Timestamp:");
        JTextField timestampField = new JTextField("2025-10-28T19:00:00Z");

        JLabel remarksLabel = new JLabel("Remarks:");
        JTextArea remarksArea = new JTextArea("Application received and logged in system");
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        JScrollPane remarksScroll = new JScrollPane(remarksArea);

        JButton submitBtn = new JButton("Submit");

        // Layout grid
        gbc.gridx = 0; gbc.gridy = 0; add(loanIdLabel, gbc);
        gbc.gridx = 1; add(loanIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; add(applicantIdLabel, gbc);
        gbc.gridx = 1; add(applicantIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; add(statusLabel, gbc);
        gbc.gridx = 1; add(statusCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; add(updatedByLabel, gbc);
        gbc.gridx = 1; add(updatedByField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; add(timestampLabel, gbc);
        gbc.gridx = 1; add(timestampField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.NORTH;
        add(remarksLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 5; gbc.weighty = 1;
        add(remarksScroll, gbc);

        gbc.gridx = 1; gbc.gridy = 6; gbc.weighty = 0; gbc.anchor = GridBagConstraints.CENTER;
        add(submitBtn, gbc);

        // Submit action -> Send JSON to Mule
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String jsonInputString = "{"
                            + "\"loanId\": \"" + loanIdField.getText() + "\","
                            + "\"applicantId\": \"" + applicantIdField.getText() + "\","
                            + "\"status\": \"" + statusCombo.getSelectedItem() + "\","
                            + "\"updatedBy\": \"" + updatedByField.getText() + "\","
                            + "\"timestamp\": \"" + timestampField.getText() + "\","
                            + "\"remarks\": \"" + remarksArea.getText() + "\""
                            + "}";

                    URL url = new URL("https://loan-application-x-api-zr9fp9.5sc6y6-1.usa-e2.cloudhub.io/api/loan/status/update");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");

                    // ✅ Client ID & Secret for MuleSoft API
                    conn.setRequestProperty("client_id", "0c5ff173846546a39c6080878a0915b0");
                    conn.setRequestProperty("client_secret", "25eB4d4027bc4EC1Ad5A9DC5C36ceF2d");

                    conn.setDoOutput(true);

                    // Send JSON body
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    // Response Code
                    int responseCode = conn.getResponseCode();
                    System.out.println("\nHTTP Response Code: " + responseCode);

                    // ✅ Read success or error response stream
                    InputStream stream = (responseCode >= 200 && responseCode < 300)
                            ? conn.getInputStream()
                            : conn.getErrorStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine;

                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    System.out.println("------ Mule Response ------");
                    System.out.println(response.toString());
                    System.out.println("---------------------------");

                    if (responseCode >= 200 && responseCode < 300) {
                        JOptionPane.showMessageDialog(null, "✅ Data Sent Successfully!");
                    } else {
                        JOptionPane.showMessageDialog(null, "❌ Error From Mule:\n" + response.toString());
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to send data: " + ex.getMessage());
                }
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new LoanStatusForm();
    }
}
