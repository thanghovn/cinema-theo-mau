package quanlyrapphim;

import java.awt.Font;
import java.sql.*;
import javax.swing.*;

public class Login extends JFrame {

    static String useNameLogin;
    // Cột admin: 2 = admin chính, 1 = admin phụ, 0 = nhân viên, -1 = chưa được cấp quyền
    static int admin;
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;

    JTextField txt_usename = new JTextField();
    JPasswordField txt_password = new JPasswordField();
    JButton btn_login = new JButton("Đăng nhập");
    JButton btn_cancel = new JButton("Thoát");

    public Login() {
        initComponents();
        conn = JavaConnect.ConnectDB();
    }

    private void initComponents() {
        setTitle("Đăng nhập - Quản lý rạp phim");
        setSize(380, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel lbTieuDe = new JLabel("QUẢN LÝ RẠP PHIM");
        lbTieuDe.setFont(new Font("Tahoma", Font.BOLD, 20));
        lbTieuDe.setBounds(90, 15, 250, 30);
        add(lbTieuDe);

        JLabel lbUser = new JLabel("Tài khoản:");
        lbUser.setBounds(40, 65, 90, 25);
        add(lbUser);
        txt_usename.setBounds(130, 65, 190, 25);
        add(txt_usename);

        JLabel lbPass = new JLabel("Mật khẩu:");
        lbPass.setBounds(40, 105, 90, 25);
        add(lbPass);
        txt_password.setBounds(130, 105, 190, 25);
        add(txt_password);

        btn_login.setBounds(130, 150, 100, 30);
        add(btn_login);
        btn_cancel.setBounds(240, 150, 80, 30);
        add(btn_cancel);

        btn_login.addActionListener(e -> btn_loginActionPerformed());
        txt_password.addActionListener(e -> btn_loginActionPerformed()); // nhấn Enter ở ô mật khẩu
        btn_cancel.addActionListener(e -> System.exit(0));
    }

    private void btn_loginActionPerformed() {
        String sql = "select * from login where username=? and password=?";
        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, txt_usename.getText().trim());
            pst.setString(2, new String(txt_password.getPassword()));
            rs = pst.executeQuery();
            if (rs.next()) {
                useNameLogin = rs.getString("username");
                admin = rs.getInt("admin");
                rs.close();
                pst.close();
                JOptionPane.showMessageDialog(null, "Đăng nhập thành công !");
                if (admin == -1) {
                    new ChoCapQuyen().setVisible(true);
                } else {
                    new MainActivity().setVisible(true);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(null, "Tài khoản hoặc mật khẩu không đúng !");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
    }
}
