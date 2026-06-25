import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new FileStore().ensureDefaults();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Failed to initialize default data.");
                }
                new LoginFrame().setVisible(true);
            }
        });
    }
}

class LoginFrame extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JComboBox<String> roleBox;
    private final FileStore fileStore;

    LoginFrame() {
        super("Blisskery - Login");
        this.fileStore = new FileStore();
        this.usernameField = new JTextField();
        this.passwordField = new JPasswordField();
        this.roleBox = new JComboBox<>(new String[] {"CUSTOMER", "ADMIN"});

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 320);
        setMinimumSize(new Dimension(500, 300));
        setLocationRelativeTo(null);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        JLabel title = new JLabel("Blisskery", JLabel.CENTER);
        UiTheme.styleTitleLabel(title);
        JLabel subtitle = new JLabel("Restaurant Ordering & Cooking Challenge", JLabel.CENTER);
        UiTheme.styleSubtitleLabel(subtitle);
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(18, 12, 8, 12));
        header.add(title);
        header.add(subtitle);

        JPanel form = new JPanel(new GridBagLayout());
        UiTheme.styleCardPanel(form);
        JLabel userLabel = new JLabel("Username:");
        JLabel passLabel = new JLabel("Password:");
        JLabel roleLabel = new JLabel("Role:");
        UiTheme.styleLabel(userLabel);
        UiTheme.styleLabel(passLabel);
        UiTheme.styleLabel(roleLabel);
        UiTheme.styleField(usernameField);
        UiTheme.styleField(passwordField);
        roleBox.setFont(UiTheme.BODY_FONT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        form.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        form.add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        form.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        form.add(roleLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        form.add(roleBox, gbc);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        UiTheme.stylePrimaryButton(loginButton);
        UiTheme.styleSecondaryButton(registerButton);
        loginButton.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        registerButton.setIcon(UIManager.getIcon("OptionPane.questionIcon"));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        actions.setOpaque(false);
        actions.add(registerButton);
        actions.add(loginButton);

        JPanel card = new JPanel(new BorderLayout());
        UiTheme.styleCardPanel(card);
        card.add(form, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        card.setBorder(BorderFactory.createCompoundBorder(
            card.getBorder(),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints centerGbc = new GridBagConstraints();
        centerGbc.gridx = 0;
        centerGbc.gridy = 0;
        centerGbc.weightx = 1.0;
        centerGbc.weighty = 1.0;
        centerGbc.anchor = GridBagConstraints.NORTH;
        centerGbc.insets = new Insets(0, 24, 12, 24);
        center.add(card, centerGbc);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                handleLogin();
            }
        });
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                handleRegister();
            }
        });
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both username and password.");
            return;
        }

        UserList users;
        try {
            users = fileStore.loadUsers();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load users data: " + ex.getMessage());
            return;
        }

        int index = 0;
        while (index < users.size()) {
            User user = users.get(index);
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                openNextScreen(user);
                return;
            }
            index += 1;
        }

        JOptionPane.showMessageDialog(this, "Invalid credentials.");
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleBox.getSelectedItem();
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both username and password.");
            return;
        }

        UserList users;
        try {
            users = fileStore.loadUsers();
        } catch (IOException ignored) {
            users = new UserList(200);
        }

        int index = 0;
        while (index < users.size()) {
            User user = users.get(index);
            if (user.getUsername().equals(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists.");
                return;
            }
            index += 1;
        }

        users.add(new User(username, password, role));
        try {
            fileStore.saveUsers(users);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save users data: " + ex.getMessage());
            return;
        }

        JOptionPane.showMessageDialog(this, "Registration successful. Please login.");
    }

    private void openNextScreen(User user) {
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            new AdminFrame(user).setVisible(true);
        } else {
            new CustomerFrame(user).setVisible(true);
        }
        dispose();
    }
}

class CustomerFrame extends JFrame {
    private static final double DISCOUNT_RATE = 0.10;

    private MenuItemList menuItems;
    private final MenuItemList cart;
    private final JTextArea cartArea;
    private final JLabel totalLabel;
    private final JButton checkoutButton;
    private final JButton clearCartButton;
    private final JButton refreshMenuButton;
    private final JButton logoutButton;
    private final JTextArea receiptArea;
    private final JPanel menuContainer;
    private final JScrollPane menuScroll;

    CustomerFrame(User user) {
        super("Blisskery - Welcome ");
        this.menuItems = loadMenuItems();
        this.cart = new MenuItemList(200);
        this.cartArea = new JTextArea(10, 18);
        this.totalLabel = new JLabel("Total: $0.00");
        this.checkoutButton = new JButton("Checkout");
        this.clearCartButton = new JButton("Clear Cart");
        this.refreshMenuButton = new JButton("Refresh Menu");
        this.logoutButton = new JButton("Logout");
        this.receiptArea = new JTextArea(6, 40);
        this.menuContainer = new JPanel();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 520);
        setMinimumSize(new java.awt.Dimension(740, 480));
        setResizable(true);
        setLocationRelativeTo(null);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        JLabel header = new JLabel("Welcome, " + user.getUsername() + "", JLabel.LEFT);
        UiTheme.styleTitleLabel(header);
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);
        UiTheme.styleSecondaryButton(refreshMenuButton);
        UiTheme.styleGhostButton(logoutButton);
        headerActions.add(refreshMenuButton);
        headerActions.add(logoutButton);
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        headerBar.setBorder(BorderFactory.createEmptyBorder(18, 18, 12, 18));
        headerBar.add(header, BorderLayout.WEST);
        headerBar.add(headerActions, BorderLayout.EAST);
        add(headerBar, BorderLayout.NORTH);

        menuContainer.setLayout(new javax.swing.BoxLayout(menuContainer, javax.swing.BoxLayout.Y_AXIS));
        menuContainer.setOpaque(false);
        this.menuScroll = new JScrollPane(menuContainer);
        UiTheme.styleScrollPane(menuScroll);
        add(menuScroll, BorderLayout.CENTER);
        refreshMenu();

        cartArea.setEditable(false);
        UiTheme.styleTextArea(cartArea);
        JPanel cartPanel = new JPanel(new BorderLayout());
        UiTheme.styleCardPanel(cartPanel);
        JLabel cartLabel = new JLabel("Cart");
        UiTheme.styleLabel(cartLabel);
        cartPanel.add(cartLabel, BorderLayout.NORTH);
        cartPanel.add(new JScrollPane(cartArea), BorderLayout.CENTER);
        JPanel cartFooter = new JPanel(new BorderLayout());
        cartFooter.setOpaque(false);
        UiTheme.styleLabel(totalLabel);
        UiTheme.styleSecondaryButton(clearCartButton);
        UiTheme.stylePrimaryButton(checkoutButton);
        cartFooter.add(totalLabel, BorderLayout.NORTH);
        JPanel cartButtons = new JPanel(new GridLayout(2, 1, 8, 8));
        cartButtons.setOpaque(false);
        cartButtons.add(clearCartButton);
        cartButtons.add(checkoutButton);
        cartFooter.add(cartButtons, BorderLayout.SOUTH);
        cartPanel.add(cartFooter, BorderLayout.SOUTH);
        add(cartPanel, BorderLayout.EAST);

        receiptArea.setEditable(false);
        UiTheme.styleTextArea(receiptArea);
        JPanel receiptPanel = new JPanel(new BorderLayout());
        UiTheme.styleCardPanel(receiptPanel);
        JLabel receiptLabel = new JLabel("Receipt");
        UiTheme.styleLabel(receiptLabel);
        receiptPanel.add(receiptLabel, BorderLayout.NORTH);
        receiptPanel.add(new JScrollPane(receiptArea), BorderLayout.CENTER);
        add(receiptPanel, BorderLayout.SOUTH);

        checkoutButton.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
        clearCartButton.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
        refreshMenuButton.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        logoutButton.setIcon(UIManager.getIcon("OptionPane.questionIcon"));
        checkoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                handleCheckout();
            }
        });
        clearCartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                clearCart();
            }
        });
        refreshMenuButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                refreshMenu();
            }
        });
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                logout();
            }
        });
    }

    private MenuItemList loadMenuItems() {
        FileStore store = new FileStore();
        try {
            return store.loadMenuItems();
        } catch (IOException ex) {
            return new MenuItemList(200);
        }
    }

    private void addToCart(MenuItem item) {
        cart.add(item);
        cartArea.append(formatCartLine(item) + System.lineSeparator());
        updateTotal();
    }

    private void updateTotal() {
        double total = 0.0;
        int index = 0;
        while (index < cart.size()) {
            total += cart.get(index).getPrice();
            index += 1;
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private double calculateTotal() {
        double total = 0.0;
        int index = 0;
        while (index < cart.size()) {
            total += cart.get(index).getPrice();
            index += 1;
        }
        return total;
    }

    private void handleCheckout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty.");
            return;
        }
        new CookingChallengeFrame(this).setVisible(true);
    }

    private void clearCart() {
        cart.clear();
        cartArea.setText("");
        updateTotal();
    }

    private void refreshMenu() {
        menuItems = loadMenuItems();
        menuContainer.removeAll();
        if (menuItems.size() == 0) {
            JLabel empty = new JLabel("No menu items available.");
            UiTheme.styleLabel(empty);
            menuContainer.add(wrapSection(empty));
        } else {
            int max = menuItems.size();
            String[] categories = new String[max];
            MenuItemList[] groups = new MenuItemList[max];
            int groupCount = 0;

            int itemIndex = 0;
            while (itemIndex < menuItems.size()) {
                MenuItem item = menuItems.get(itemIndex);
                String category = item.getCategory();
                if (category == null || category.length() == 0) {
                    category = "General";
                }
                groupCount = addToCategory(categories, groups, groupCount, category, item, max);
                itemIndex += 1;
            }

            int groupIndex = 0;
            while (groupIndex < groupCount) {
                JPanel sectionPanel = buildCategorySection(categories[groupIndex], groups[groupIndex]);
                menuContainer.add(sectionPanel);
                groupIndex += 1;
            }
        }
        menuContainer.revalidate();
        menuContainer.repaint();
    }

    private int addToCategory(String[] categories, MenuItemList[] groups, int groupCount, String category, MenuItem item, int max) {
        int index = 0;
        while (index < groupCount) {
            if (categories[index].equals(category)) {
                groups[index].add(item);
                return groupCount;
            }
            index += 1;
        }
        if (groupCount < max) {
            categories[groupCount] = category;
            groups[groupCount] = new MenuItemList(max);
            groups[groupCount].add(item);
            return groupCount + 1;
        }
        return groupCount;
    }

    private JPanel buildCategorySection(String category, MenuItemList items) {
        JLabel header = new JLabel(category);
        UiTheme.styleSectionLabel(header);

        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 10));
        grid.setOpaque(false);
        int index = 0;
        while (index < items.size()) {
            MenuItem item = items.get(index);
            JButton itemButton = new JButton(formatMenuLabel(item));
            UiTheme.styleMenuButton(itemButton);
            itemButton.setBackground(UiTheme.Pink);
            itemButton.setOpaque(true);
            itemButton.setContentAreaFilled(true);
            itemButton.setPreferredSize(new Dimension(220, 56));
            itemButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    addToCart(item);
                }
            });
            grid.add(itemButton);
            index += 1;
        }

        JPanel section = new JPanel(new BorderLayout(8, 8));
        UiTheme.styleCardPanel(section);
        section.add(header, BorderLayout.NORTH);
        section.add(grid, BorderLayout.CENTER);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setBorder(BorderFactory.createCompoundBorder(
            section.getBorder(),
            BorderFactory.createEmptyBorder(0, 0, 12, 0)
        ));
        return section;
    }

    private JPanel wrapSection(JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        UiTheme.styleCardPanel(panel);
        panel.add(component, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private void logout() {
        new LoginFrame().setVisible(true);
        dispose();
    }

    void completeCheckout(boolean success) {
        double subtotal = calculateTotal();
        double discountAmount = success ? subtotal * DISCOUNT_RATE : 0.0;
        double finalTotal = subtotal - discountAmount;
        receiptArea.setText(buildReceiptText(subtotal, discountAmount, finalTotal));

        cart.clear();
        cartArea.setText("");
        updateTotal();
        if (success) {
            JOptionPane.showMessageDialog(this, "Order placed! Great cooking.");
        } else {
            JOptionPane.showMessageDialog(this, "Order placed. No discount applied.");
        }
    }

    private String buildReceiptText(double subtotal, double discountAmount, double finalTotal) {
        StringBuilder builder = new StringBuilder();
        builder.append("Blisskery Receipt").append(System.lineSeparator());
        builder.append("----------------------").append(System.lineSeparator());
        int index = 0;
        while (index < cart.size()) {
            builder.append(formatReceiptLine(cart.get(index))).append(System.lineSeparator());
            index += 1;
        }
        builder.append("Subtotal: $")
            .append(String.format("%.2f", subtotal))
            .append(System.lineSeparator());
        if (discountAmount > 0.0) {
            builder.append("Discount (10%): -$")
                .append(String.format("%.2f", discountAmount))
                .append(System.lineSeparator());
        } else {
            builder.append("Discount (0%): -$0.00").append(System.lineSeparator());
        }
        builder.append("Total: $")
            .append(String.format("%.2f", finalTotal))
            .append(System.lineSeparator());
        return builder.toString();
    }

    private String formatMenuLabel(MenuItem item) {
        return item.getName() + " (" + item.getCategory() + ") - $" + String.format("%.2f", item.getPrice());
    }

    private String formatCartLine(MenuItem item) {
        return item.getName() + " (" + item.getCategory() + ") - $" + String.format("%.2f", item.getPrice());
    }

    private String formatReceiptLine(MenuItem item) {
        return item.getName() + " (" + item.getCategory() + ") - $" + String.format("%.2f", item.getPrice());
    }
}

class CookingChallengeFrame extends JFrame {
    private static final int TIME_LIMIT_SECONDS = 10 ;

    private final CustomerFrame parent;
    private final JLabel infoLabel;
    private final JLabel questionLabel;
    private final JLabel timeLabel;
    private final JTextField answerField;
    private final JButton submitButton;
    private final Timer timer;
    private int remainingSeconds;
    private int expectedAnswer;

    CookingChallengeFrame(CustomerFrame parent) {
        super("Speed Challenge");
        this.parent = parent;
        this.infoLabel = new JLabel("Speed Challenge", JLabel.CENTER);
        this.questionLabel = new JLabel("", JLabel.CENTER);
        this.timeLabel = new JLabel("Time: " + TIME_LIMIT_SECONDS + "s", JLabel.CENTER);
        this.answerField = new JTextField();
        this.submitButton = new JButton("Submit");
        this.remainingSeconds = TIME_LIMIT_SECONDS;
        this.expectedAnswer = 0;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 240);
        setLocationRelativeTo(parent);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);
        UiTheme.styleLabel(infoLabel);
        UiTheme.styleLabel(questionLabel);
        UiTheme.styleLabel(timeLabel);
        UiTheme.styleField(answerField);
        UiTheme.stylePrimaryButton(submitButton);
        root.add(infoLabel, BorderLayout.NORTH);
        root.add(questionLabel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);
        bottom.add(timeLabel, BorderLayout.NORTH);
        bottom.add(answerField, BorderLayout.CENTER);
        bottom.add(submitButton, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                handleSubmit();
            }
        });
        answerField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                handleSubmit();
            }
        });

        generateSequence();

        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                tick();
            }
        });
        timer.start();
    }

    private void handleSubmit() {
        String text = answerField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        int answer;
        try {
            answer = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            finish(false);
            return;
        }
        finish(answer == expectedAnswer);
    }

    private void tick() {
        remainingSeconds -= 1;
        timeLabel.setText("Time: " + remainingSeconds + "s");
        if (remainingSeconds <= 0) {
            finish(false);
        }
    }

    private void finish(boolean success) {
        timer.stop();
        submitButton.setEnabled(false);
        answerField.setEnabled(false);
        parent.completeCheckout(success);
        dispose();
    }

    private void generateSequence() {
        Random random = new Random();
        int a = 2 + random.nextInt(9);
        int b = 2 + random.nextInt(9);
        int c = 2 + random.nextInt(9);
        expectedAnswer = a + (b * c);
        questionLabel.setText(a + " + " + b + " x " + c + " = ?");
        answerField.setText("");
    }
}

class AdminFrame extends JFrame {
    private final JTextField nameField;
    private final JTextField priceField;
    private final JTextField categoryField;
    private final JTextField deleteField;
    private final JTextArea statusArea;
    private final JButton logoutButton;

    AdminFrame(User user) {
        super("Admin Dashboard");
        this.nameField = new JTextField();
        this.priceField = new JTextField();
        this.categoryField = new JTextField();
        this.deleteField = new JTextField();
        this.statusArea = new JTextArea(4, 28);
        this.logoutButton = new JButton("Logout");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(620, 360);
        setMinimumSize(new java.awt.Dimension(560, 340));
        setResizable(true);
        setLocationRelativeTo(null);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        JLabel header = new JLabel("Admin Kitchen", JLabel.LEFT);
        UiTheme.styleTitleLabel(header);
        UiTheme.styleGhostButton(logoutButton);
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        headerBar.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        headerBar.add(header, BorderLayout.WEST);
        headerBar.add(logoutButton, BorderLayout.EAST);
        add(headerBar, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        UiTheme.styleCardPanel(form);
        JLabel nameLabel = new JLabel("Dish name:");
        JLabel priceLabel = new JLabel("Price:");
        JLabel categoryLabel = new JLabel("Category:");
        JLabel deleteLabel = new JLabel("Delete by name:");
        UiTheme.styleLabel(nameLabel);
        UiTheme.styleLabel(priceLabel);
        UiTheme.styleLabel(categoryLabel);
        UiTheme.styleLabel(deleteLabel);
        UiTheme.styleField(nameField);
        UiTheme.styleField(priceField);
        UiTheme.styleField(categoryField);
        UiTheme.styleField(deleteField);
        form.add(nameLabel);
        form.add(nameField);
        form.add(priceLabel);
        form.add(priceField);
        form.add(categoryLabel);
        form.add(categoryField);
        form.add(deleteLabel);
        form.add(deleteField);

        JButton addButton = new JButton("Add to Menu");
        JButton deleteButton = new JButton("Delete Item");
        UiTheme.stylePrimaryButton(addButton);
        UiTheme.styleSecondaryButton(deleteButton);
        addButton.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        deleteButton.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);
        actions.add(deleteButton);
        actions.add(addButton);

        statusArea.setEditable(false);
        UiTheme.styleTextArea(statusArea);
        JPanel statusPanel = new JPanel(new BorderLayout());
        UiTheme.styleCardPanel(statusPanel);
        JLabel statusLabel = new JLabel("Status");
        UiTheme.styleLabel(statusLabel);
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        center.add(form, BorderLayout.CENTER);
        center.add(actions, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.EAST);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                handleAddMenuItem();
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                handleDeleteMenuItem();
            }
        });
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                logout();
            }
        });
    }

    private void handleAddMenuItem() {
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();
        String category = categoryField.getText().trim();
        if (name.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a dish name and price.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price must be a number.");
            return;
        }

        if (price < 0.0) {
            JOptionPane.showMessageDialog(this, "Price must be positive.");
            return;
        }

        if (category.isEmpty()) {
            category = "General";
        }

        FileStore store = new FileStore();
        MenuItemList items;
        try {
            items = store.loadMenuItems();
        } catch (IOException ex) {
            items = new MenuItemList(200);
        }

        items.add(new MenuItem(name, price, category));
        try {
            store.saveMenuItems(items);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save menu data: " + ex.getMessage());
            return;
        }

        statusArea.append("Added: " + name + " ($" + String.format("%.2f", price) + ")" + System.lineSeparator());
        nameField.setText("");
        priceField.setText("");
        categoryField.setText("");
    }

    private void handleDeleteMenuItem() {
        String targetName = deleteField.getText().trim();
        if (targetName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a dish name to delete.");
            return;
        }

        FileStore store = new FileStore();
        MenuItemList items;
        try {
            items = store.loadMenuItems();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load menu file.");
            return;
        }

        MenuItemList filtered = new MenuItemList(items.capacity());
        boolean removed = false;
        int index = 0;
        while (index < items.size()) {
            MenuItem item = items.get(index);
            if (item.getName().equalsIgnoreCase(targetName)) {
                removed = true;
            } else {
                filtered.add(item);
            }
            index += 1;
        }
        if (!removed) {
            JOptionPane.showMessageDialog(this, "No matching item found.");
            return;
        }

        try {
            store.saveMenuItems(filtered);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save menu data: " + ex.getMessage());
            return;
        }

        statusArea.append("Deleted: " + targetName + System.lineSeparator());
        deleteField.setText("");
    }

    private void logout() {
        new LoginFrame().setVisible(true);
        dispose();
    }
}

class GradientPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        GradientPaint paint = new GradientPaint(0, 0, UiTheme.CREAM, 0, getHeight(), UiTheme.MIST);
        g2.setPaint(paint);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}

class UiTheme {
    // Baby-pink theme colors
    static final Color CREAM = new Color(255, 240, 245);      // very pale pink
    static final Color MIST = new Color(255, 223, 233);       // soft pink gradient end
    static final Color INK = new Color(60, 20, 40);           // deep contrast color for text
    static final Color SAGE = new Color(255, 102, 153);       // primary pink (used for primary buttons)
    static final Color GOLD = new Color(255, 182, 193);       // secondary pastel pink (used for secondary buttons)
    static final Color CARD = new Color(255, 250, 252);       // card background with slight pink tint
    static final Color Pink = new Color(255, 240, 245);     

    static final Font TITLE_FONT = new Font("Georgia", Font.BOLD, 22);
    static final Font SUBTITLE_FONT = new Font("Georgia", Font.PLAIN, 13);
    static final Font BODY_FONT = new Font("Georgia", Font.PLAIN, 14);
    static final Font SMALL_FONT = new Font("Georgia", Font.PLAIN, 12);

    static void styleTitleLabel(JLabel label) {
        label.setFont(TITLE_FONT);
        label.setForeground(INK);
    }

    static void styleSubtitleLabel(JLabel label) {
        label.setFont(SUBTITLE_FONT);
        label.setForeground(SAGE.darker());
    }

    static void styleLabel(JLabel label) {
        label.setFont(BODY_FONT);
        label.setForeground(INK);
    }

    static void styleSectionLabel(JLabel label) {
        label.setFont(new Font("Georgia", Font.BOLD, 14));
        label.setForeground(SAGE.darker());
    }

    static void styleField(JTextField field) {
        field.setFont(BODY_FONT);
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(220, 28));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MIST.darker(), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    static void styleTextArea(JTextArea area) {
        area.setFont(SMALL_FONT);
        area.setBackground(Color.WHITE);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MIST.darker(), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
    }

    static void styleCardPanel(JPanel panel) {
        panel.setOpaque(true);
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MIST.darker(), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
    }

    static void styleScrollPane(JScrollPane pane) {
        pane.setBorder(BorderFactory.createEmptyBorder(8, 16, 12, 16));
        pane.getViewport().setBackground(CARD);
    }

    static void stylePrimaryButton(JButton button) {
        button.setFont(BODY_FONT);
        button.setBackground(SAGE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SAGE.darker(), 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    static void styleSecondaryButton(JButton button) {
        button.setFont(BODY_FONT);
        button.setBackground(GOLD);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GOLD.darker(), 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    static void styleGhostButton(JButton button) {
        button.setFont(BODY_FONT);
        button.setBackground(CREAM);
        button.setForeground(INK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(MIST.darker(), 1));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    static void styleMenuButton(JButton button) {
        button.setFont(SMALL_FONT);
        button.setBackground(CARD);
        button.setForeground(INK);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MIST.darker(), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}

class UserList {
    private final User[] items;
    private int count;

    UserList(int capacity) {
        items = new User[capacity];
        count = 0;
    }

    void add(User user) {
        if (count < items.length) {
            items[count] = user;
            count += 1;
        }
    }

    User get(int index) {
        return items[index];
    }

    int size() {
        return count;
    }

    int capacity() {
        return items.length;
    }
}

class MenuItemList {
    private final MenuItem[] items;
    private int count;

    MenuItemList(int capacity) {
        items = new MenuItem[capacity];
        count = 0;
    }

    void add(MenuItem item) {
        if (count < items.length) {
            items[count] = item;
            count += 1;
        }
    }

    MenuItem get(int index) {
        return items[index];
    }

    int size() {
        return count;
    }

    boolean isEmpty() {
        return count == 0;
    }

    void clear() {
        count = 0;
    }

    int capacity() {
        return items.length;
    }
}
