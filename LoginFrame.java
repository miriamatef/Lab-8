package skillforge;

import javax.swing.*;
import java.awt.*;
import org.json.JSONObject;

public class LoginFrame extends JFrame {
    private final UserService userService;

    public LoginFrame(UserService userService) {
        super("SkillForge - Login");
        this.userService = userService;
        init();
        checkLoggedInUser(); 
    }

    private void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 220);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel le = new JLabel("Email:");
        JTextField te = new JTextField(18);
        JLabel lp = new JLabel("Password:");
        JPasswordField tp = new JPasswordField(18);
        JButton btnLogin = new JButton("Login");
        JButton btnSignup = new JButton("Signup");

        c.gridx = 0; c.gridy = 0; panel.add(le, c);
        c.gridx = 1; panel.add(te, c);
        c.gridx = 0; c.gridy = 1; panel.add(lp, c);
        c.gridx = 1; panel.add(tp, c);
        c.gridx = 1; c.gridy = 2; panel.add(btnLogin, c);
        c.gridx = 1; c.gridy = 3; panel.add(btnSignup, c);

        btnLogin.addActionListener(e -> {
            String email = te.getText().trim();
            String password = new String(tp.getPassword());
            System.out.println("Login clicked with email: " + email);

            JSONObject user = userService.loginByEmail(email, password);
            System.out.println("Login result: " + user);

            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid email or password",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            openDashboard(user);
        });

        btnSignup.addActionListener(e -> {
            new SignupFrame(userService).setVisible(true);
            dispose();
        });

        add(panel);
    }
//hena zawedt lel admin dashboard
    private void openDashboard(JSONObject user) {
        String role = user.optString("role", "");
        String uid = user.optString("id", "");
        System.out.println("Opening dashboard for role: " + role);

        SwingUtilities.invokeLater(() -> {
            if ("instructor".equalsIgnoreCase(role)) {
                new InstructorDashboardFrame(uid, userService, this).setVisible(true);
            } else if ("student".equalsIgnoreCase(role)) {
                new StudentDashboardFrame(uid, this).setVisible(true);
            } else if ("admin".equalsIgnoreCase(role)) {
                try {
                    JsonDatabaseManager db = new JsonDatabaseManager();
                    AdminManager adminManager = new AdminManager(db);
                    AdminDashboardFrame dash = new AdminDashboardFrame(adminManager);
                    dash.setUserService(userService); 
                    dash.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to open admin dashboard.",
                            "Dashboard Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,"", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
            setVisible(false);
        });
    }

    private void checkLoggedInUser() {
        JSONObject user = userService.getLoggedInUser();
        if (user != null) {
            openDashboard(user);
        }
    }
}
