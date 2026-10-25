package quanlyrapphim;

import javax.swing.*;

// Màn chờ cho tài khoản chưa được cấp quyền (admin = -1): chỉ đổi mật khẩu và đăng xuất
public class ChoCapQuyen extends JFrame {

    public ChoCapQuyen() {
        setTitle("Chờ cấp quyền - Quản lý rạp phim");
        setSize(400, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel lb = new JLabel("<html>Xin chào: " + Login.useNameLogin
                + "<br>Tài khoản chưa được cấp quyền, hãy liên hệ admin.</html>");
        lb.setBounds(15, 10, 270, 40);
        add(lb);
        JButton btnDangXuat = new JButton("Đăng xuất");
        btnDangXuat.setBounds(285, 15, 95, 30);
        add(btnDangXuat);
        btnDangXuat.addActionListener(e -> {
            int p = JOptionPane.showConfirmDialog(null, "Bạn có chắc muốn đăng xuất?", "Đăng xuất", JOptionPane.YES_NO_OPTION);
            if (p == 0) {
                dispose();
                new Login().setVisible(true);
            }
        });

        // Dùng lại phần đổi mật khẩu của màn hình Tài khoản (phần admin tự ẩn vì admin = -1)
        JDesktopPane desktop = new JDesktopPane();
        desktop.setBounds(0, 60, 400, 260);
        add(desktop);
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setBounds(0, 0, 385, 255);
        desktop.add(taiKhoan);
        taiKhoan.setVisible(true);
    }
}
