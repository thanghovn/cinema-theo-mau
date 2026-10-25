package quanlyrapphim;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class MainActivity extends JFrame {

    int admin;
    Phim phim = null;
    SuatChieu suatChieu = null;
    BanVe banVe = null;
    TaiKhoan taiKhoan = null;
    Timer timer;

    JDesktopPane jDestopTable = new JDesktopPane();
    JLabel labelUseName = new JLabel();
    JLabel labelTime = new JLabel();

    public MainActivity() {
        initComponents();
        admin = Login.admin;
        labelUseName.setText("Xin chào: " + Login.useNameLogin + (admin == 2 ? " (admin chính)" : admin == 1 ? " (admin phụ)" : " (nhân viên)"));
        displayAllTable();

        // Đồng hồ: cứ 1000 ms (1 giây) cập nhật giờ một lần
        timer = new Timer(1000, e -> {
            labelTime.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        });
        timer.start();
    }

    private void initComponents() {
        setTitle("Quản lý rạp phim");
        setSize(1000, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar jMenuBar1 = new JMenuBar();
        JMenu jMenuChucNang = new JMenu("Chức năng");
        JMenuItem jMenuPhim = new JMenuItem("Phim");
        JMenuItem jMenuSuatChieu = new JMenuItem("Suất chiếu");
        JMenuItem jMenuBanVe = new JMenuItem("Bán vé");
        JMenuItem jMenuTaiKhoan = new JMenuItem("Tài khoản");
        JMenuItem jMenuDangXuat = new JMenuItem("Đăng xuất");
        jMenuChucNang.add(jMenuPhim);
        jMenuChucNang.add(jMenuSuatChieu);
        jMenuChucNang.add(jMenuBanVe);
        jMenuChucNang.add(jMenuTaiKhoan);
        jMenuChucNang.addSeparator();
        jMenuChucNang.add(jMenuDangXuat);
        jMenuBar1.add(jMenuChucNang);
        jMenuBar1.add(Box.createHorizontalGlue());
        jMenuBar1.add(labelUseName);
        jMenuBar1.add(new JLabel("   |   "));
        jMenuBar1.add(labelTime);
        jMenuBar1.add(new JLabel("   "));
        JButton btnDangXuat = new JButton("Đăng xuất");
        btnDangXuat.addActionListener(e -> lbDangXuatMouseClicked());
        jMenuBar1.add(btnDangXuat);
        setJMenuBar(jMenuBar1);

        add(jDestopTable);

        jMenuPhim.addActionListener(e -> hienThi(phim));
        jMenuSuatChieu.addActionListener(e -> {
            suatChieu.fillComboMaPhim(); // phim mới thêm thì combo phải có
            hienThi(suatChieu);
        });
        jMenuBanVe.addActionListener(e -> {
            banVe.fillComboMaSuat();
            hienThi(banVe);
        });
        jMenuTaiKhoan.addActionListener(e -> hienThi(taiKhoan));
        jMenuDangXuat.addActionListener(e -> lbDangXuatMouseClicked());
    }

    // Tạo sẵn các cửa sổ con, giống displayAllTable() của bài mẫu
    private void displayAllTable() {
        phim = new Phim();
        suatChieu = new SuatChieu();
        banVe = new BanVe();
        taiKhoan = new TaiKhoan();
        jDestopTable.add(phim);
        jDestopTable.add(suatChieu);
        jDestopTable.add(banVe);
        jDestopTable.add(taiKhoan);
        hienThi(phim);
    }

    // Hiện 1 cửa sổ con, ẩn các cửa sổ còn lại
    private void hienThi(JInternalFrame f) {
        phim.setVisible(false);
        suatChieu.setVisible(false);
        banVe.setVisible(false);
        taiKhoan.setVisible(false);
        f.setVisible(true);
    }

    private void lbDangXuatMouseClicked() {
        int p = JOptionPane.showConfirmDialog(null, "Bạn có chắc muốn đăng xuất?", "Đăng xuất", JOptionPane.YES_NO_OPTION);
        if (p != 0) {
            return;
        }
        timer.stop();
        dispose();
        new Login().setVisible(true);
    }
}
