package skillforge;

import javax.swing.*;
import java.awt.*;

public class SignupFrame extends JFrame {
    private final UserService userService;

    public SignupFrame(UserService userService) {
        super("SkillForge - Signup");
        this.userService = userService;
        init();
        setVisible(true);
    }

    private void init() {
        setSize(420,300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);

        JLabel lr = new JLabel("Role:");
        JComboBox<String> cb = new JComboBox<>(new String[]{"student","instructor","admin"});
        JLabel ln = new JLabel("Username:"); JTextField tn = new JTextField(18);
        JLabel le = new JLabel("Email:"); JTextField te = new JTextField(18);
        JLabel lp = new JLabel("Password:"); JPasswordField tp = new JPasswordField(18);
        JButton btn = new JButton("Sign up");

        c.gridx=0; c.gridy=0; p.add(lr,c); c.gridx=1; p.add(cb,c);
        c.gridx=0; c.gridy=1; p.add(ln,c); c.gridx=1; p.add(tn,c);
        c.gridx=0; c.gridy=2; p.add(le,c); c.gridx=1; p.add(te,c);
        c.gridx=0; c.gridy=3; p.add(lp,c); c.gridx=1; p.add(tp,c);
        c.gridx=1; c.gridy=4; p.add(btn,c);

        btn.addActionListener(e -> {
            String role = (String)cb.getSelectedItem();
            String username = tn.getText().trim();
            String email = te.getText().trim();
            String pass = new String(tp.getPassword());
            boolean ok = userService.signup(role, username, email, pass);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Signup success. Please login.");
                new LoginFrame(userService).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Signup failed. Check inputs or email already used.");
            }
        });

        add(p);
    }
}