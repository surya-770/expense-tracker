package com.expensetracker.desktop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

/** Native Swing desktop frontend for the Ledgerly personal expense tracker. */
public final class ExpenseTrackerDesktop {
    private static final Color INK = new Color(25, 42, 38);
    private static final Color MUTED = new Color(111, 128, 121);
    private static final Color GREEN = new Color(35, 111, 83);
    private static final Color PALE = new Color(242, 246, 243);
    private static final Color LINE = new Color(226, 233, 228);
    private static final Color WHITE = Color.WHITE;
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font TITLE = new Font("Segoe UI", Font.BOLD, 27);

    private final SupabaseClient api = SupabaseClient.fromProjectConfig();
    private final ObjectMapper json = new ObjectMapper();
    private final JFrame frame = new JFrame("Ledgerly | Personal Finance");
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageHost = new JPanel(pageLayout);
    private final JLabel userLabel = new JLabel("Signed out");
    private final JLabel statusLabel = new JLabel("Ready");
    private String activePage = "Dashboard";
    private JsonNode user;
    private List<JsonNode> categories = new ArrayList<>();
    private List<JsonNode> transactions = new ArrayList<>();
    private List<JsonNode> budgets = new ArrayList<>();
    private List<JsonNode> goals = new ArrayList<>();

    private ExpenseTrackerDesktop() {}

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Keep Swing's default look and feel if the OS theme is unavailable.
            }
            new ExpenseTrackerDesktop().showLogin();
        });
    }

    private void showLogin() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1080, 700));
        frame.setSize(1280, 820);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(buildLogin());
        frame.setVisible(true);
    }

    private JPanel buildLogin() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        JPanel brand = new JPanel(new BorderLayout(20, 20));
        brand.setBackground(new Color(20, 57, 47));
        brand.setBorder(BorderFactory.createEmptyBorder(54, 54, 46, 54));
        JLabel logo = new JLabel("  L  ledgerly.");
        logo.setForeground(WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel message = new JLabel("<html><div style='width:390px'><span style='font-size:30px;font-weight:700'>Your money,<br>clearly.</span><br><br><span style='font-size:15px'>A calm space to track spending, plan a budget, and make progress toward the things you care about.</span></div></html>");
        message.setForeground(WHITE);
        message.setVerticalAlignment(SwingConstants.CENTER);
        JLabel foot = new JLabel("PRIVATE BY DESIGN  ·  CONNECTED TO SUPABASE");
        foot.setForeground(new Color(190, 220, 203));
        foot.setFont(new Font("Segoe UI", Font.BOLD, 11));
        brand.add(logo, BorderLayout.NORTH);
        brand.add(message, BorderLayout.CENTER);
        brand.add(foot, BorderLayout.SOUTH);

        JPanel formWrap = new JPanel(new GridLayout(1, 1));
        formWrap.setBackground(PALE);
        JPanel form = new JPanel();
        form.setBackground(PALE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(84, 70, 60, 70));
        JLabel eyebrow = new JLabel("PERSONAL FINANCE WORKSPACE");
        eyebrow.setForeground(GREEN); eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        JLabel title = new JLabel("Welcome back");
        title.setForeground(INK); title.setFont(TITLE);
        JLabel subtitle = new JLabel("Sign in with your Ledgerly account.");
        subtitle.setForeground(MUTED); subtitle.setFont(BODY);
        form.add(eyebrow); form.add(Box.createVerticalStrut(12)); form.add(title);
        form.add(Box.createVerticalStrut(7)); form.add(subtitle); form.add(Box.createVerticalStrut(32));

        JTextField email = field("Email address");
        JPasswordField password = new JPasswordField(); styleField(password); password.setPreferredSize(new Dimension(360, 44));
        form.add(label("Email address")); form.add(Box.createVerticalStrut(7)); form.add(email);
        form.add(Box.createVerticalStrut(18)); form.add(label("Password")); form.add(Box.createVerticalStrut(7)); form.add(password);
        form.add(Box.createVerticalStrut(22));
        JLabel error = new JLabel(" "); error.setForeground(new Color(184, 66, 67)); error.setFont(BODY); form.add(error);
        form.add(Box.createVerticalStrut(9));
        JButton signIn = primaryButton("Sign in");
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT); signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        form.add(signIn); form.add(Box.createVerticalStrut(14));
        JButton create = flatButton("Create a new account"); create.setAlignmentX(Component.LEFT_ALIGNMENT); form.add(create);
        JLabel note = new JLabel("Your financial records stay private to your account."); note.setForeground(MUTED); note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(Box.createVerticalStrut(35)); form.add(note); form.add(Box.createVerticalGlue());
        signIn.addActionListener(e -> {
            error.setText(" ");
            String address = email.getText().trim(); String secret = new String(password.getPassword());
            if (address.isBlank() || secret.isBlank()) { error.setText("Enter your email and password."); return; }
            signIn.setEnabled(false); signIn.setText("Signing in…");
            runTask(() -> user = api.signIn(address, secret), () -> { signIn.setEnabled(true); signIn.setText("Sign in"); showWorkspace(); }, ex -> {
                signIn.setEnabled(true); signIn.setText("Sign in"); error.setText(ex.getMessage());
            });
        });
        create.addActionListener(e -> showRegistration(email.getText().trim()));
        formWrap.add(form);
        root.add(brand); root.add(formWrap);
        return root;
    }

    private void showRegistration(String defaultEmail) {
        JTextField name = new JTextField(); JTextField email = new JTextField(defaultEmail);
        JPasswordField password = new JPasswordField(); JComboBox<String> currency = new JComboBox<>(new String[]{"USD","INR","EUR","GBP","CAD","AUD","JPY"});
        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(label("Full name")); panel.add(name); panel.add(label("Email")); panel.add(email);
        panel.add(label("Password (10+ chars, upper/lowercase, number and symbol)")); panel.add(password); panel.add(label("Currency")); panel.add(currency);
        if (JOptionPane.showConfirmDialog(frame, panel, "Create your Ledgerly account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        String pass = new String(password.getPassword());
        if (name.getText().isBlank() || email.getText().isBlank() || pass.isBlank()) { error("Please fill in all account fields."); return; }
        runTask(() -> user = api.register(name.getText().trim(), email.getText().trim(), pass, String.valueOf(currency.getSelectedItem())),
                this::showWorkspace, ex -> error(ex.getMessage()));
    }

    private void showWorkspace() {
        frame.getContentPane().removeAll();
        JPanel root = new JPanel(new BorderLayout()); root.setBackground(PALE);
        JPanel sidebar = new JPanel(); sidebar.setBackground(new Color(250, 252, 250)); sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, LINE)); sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel("  L  ledgerly."); logo.setFont(new Font("Segoe UI", Font.BOLD, 22)); logo.setForeground(INK); logo.setBorder(BorderFactory.createEmptyBorder(25, 12, 26, 8)); logo.setAlignmentX(Component.LEFT_ALIGNMENT); sidebar.add(logo);
        addNavSection(sidebar, "WORKSPACE", new String[]{"Dashboard","Transactions","Categories"});
        addNavSection(sidebar, "PLAN & GROW", new String[]{"Budget planner","Savings goals","Interest calculator","Reports & analytics"});
        addNavSection(sidebar, "PREFERENCES", new String[]{"Profile & settings"});
        sidebar.add(Box.createVerticalGlue());
        userLabel.setText(user.path("user_metadata").path("full_name").asText(user.path("email").asText("Account")));
        userLabel.setFont(BOLD); userLabel.setForeground(INK); userLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 4, 5)); userLabel.setAlignmentX(Component.LEFT_ALIGNMENT); sidebar.add(userLabel);
        JLabel email = new JLabel(user.path("email").asText("")); email.setFont(new Font("Segoe UI", Font.PLAIN, 12)); email.setForeground(MUTED); email.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 5)); email.setAlignmentX(Component.LEFT_ALIGNMENT); sidebar.add(email);

        JPanel main = new JPanel(new BorderLayout()); main.setBackground(PALE);
        JPanel topbar = new JPanel(new BorderLayout()); topbar.setBackground(WHITE); topbar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,LINE), BorderFactory.createEmptyBorder(16, 26, 16, 26)));
        JLabel breadcrumb = new JLabel("Workspace   /   " + activePage); breadcrumb.setForeground(MUTED); breadcrumb.setFont(BODY);
        statusLabel.setForeground(MUTED); statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topbar.add(breadcrumb, BorderLayout.WEST); topbar.add(statusLabel, BorderLayout.EAST);
        pageHost.setBackground(PALE); main.add(topbar, BorderLayout.NORTH); main.add(pageHost, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST); root.add(main, BorderLayout.CENTER);
        frame.setContentPane(root); frame.revalidate(); frame.repaint();
        refreshData(() -> showPage("Dashboard"));
    }

    private void addNavSection(JPanel side, String heading, String[] names) {
        JLabel caption = new JLabel(heading); caption.setForeground(new Color(145, 160, 152)); caption.setFont(new Font("Segoe UI", Font.BOLD, 10)); caption.setBorder(BorderFactory.createEmptyBorder(14, 20, 8, 6)); caption.setAlignmentX(Component.LEFT_ALIGNMENT); side.add(caption);
        for (String name : names) {
            JButton button = flatButton(navIcon(name) + "   " + name); button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 8)); button.setAlignmentX(Component.LEFT_ALIGNMENT); button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
            button.addActionListener(e -> showPage(name)); side.add(button);
        }
    }

    private String navIcon(String title) {
        return switch (title) { case "Dashboard" -> "▦"; case "Transactions" -> "↔"; case "Categories" -> "◫"; case "Budget planner" -> "▤"; case "Savings goals" -> "◇"; case "Interest calculator" -> "ƒx"; case "Reports & analytics" -> "▥"; default -> "◎"; };
    }

    private void showPage(String title) {
        activePage = title;
        pageHost.removeAll(); pageHost.add(switch (title) {
            case "Dashboard" -> dashboardPage(); case "Transactions" -> transactionsPage(); case "Categories" -> categoriesPage();
            case "Budget planner" -> budgetPage(); case "Savings goals" -> goalsPage(); case "Interest calculator" -> interestPage();
            case "Reports & analytics" -> reportsPage(); default -> profilePage();
        }, title);
        pageLayout.show(pageHost, title); pageHost.revalidate(); pageHost.repaint();
    }

    private JPanel pageShell(String eyebrow, String title, String desc) {
        JPanel shell = new JPanel(new BorderLayout(0, 18)); shell.setBackground(PALE); shell.setBorder(BorderFactory.createEmptyBorder(27, 30, 30, 30));
        JPanel heading = new JPanel(); heading.setOpaque(false); heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel small = new JLabel(eyebrow.toUpperCase(Locale.ROOT)); small.setForeground(new Color(113, 145, 128)); small.setFont(new Font("Segoe UI", Font.BOLD, 11));
        JLabel h1 = new JLabel(title); h1.setForeground(INK); h1.setFont(TITLE); JLabel detail = new JLabel(desc); detail.setForeground(MUTED); detail.setFont(BODY);
        heading.add(small); heading.add(Box.createVerticalStrut(6)); heading.add(h1); heading.add(Box.createVerticalStrut(4)); heading.add(detail); shell.add(heading, BorderLayout.NORTH); return shell;
    }

    private JPanel dashboardPage() {
        String first = user.path("user_metadata").path("full_name").asText("there").split(" ")[0];
        JPanel shell = pageShell("Financial overview", "Welcome, " + first, "A clear view of your income, spending, and balance.");
        JPanel body = new JPanel(new BorderLayout(0, 16)); body.setOpaque(false);
        YearMonth month = YearMonth.now(); List<JsonNode> current = transactions.stream().filter(t -> date(t).startsWith(month.toString())).toList();
        BigDecimal income = sum(current, "INCOME"), expenses = sum(current, "EXPENSE"); BigDecimal budget = budgets.stream().filter(b -> b.path("budget_month").asInt() == month.getMonthValue() && b.path("budget_year").asInt() == month.getYear()).map(b -> decimal(b,"total_budget")).findFirst().orElse(BigDecimal.ZERO);
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0)); cards.setOpaque(false);
        cards.add(metric("Income this month", money(income), "Recorded inflows")); cards.add(metric("Expenses this month", money(expenses), "Recorded outflows")); cards.add(metric("Net balance", money(allTime("INCOME").subtract(allTime("EXPENSE"))), "All-time net")); cards.add(metric("Budget remaining", budget.signum() == 0 ? "Not planned" : money(budget.subtract(expenses)), goals.size() + " savings goals"));
        JPanel lower = new JPanel(new GridLayout(1, 2, 14, 0)); lower.setOpaque(false);
        JPanel chart = card(); chart.setLayout(new BorderLayout(5, 10)); chart.add(sectionTitle("Income vs expenses", "Selected month totals"), BorderLayout.NORTH); chart.add(new BarChart(income.doubleValue(), expenses.doubleValue()), BorderLayout.CENTER);
        JPanel recent = card(); recent.setLayout(new BorderLayout(0, 8)); recent.add(sectionTitle("Recent transactions", "Latest account activity"), BorderLayout.NORTH); recent.add(table(new String[]{"DATE","DESCRIPTION","CATEGORY","AMOUNT"}, transactions.stream().limit(8).toList(), t -> new Object[]{date(t), t.path("description").asText(t.path("source").asText("—")), categoryName(t), money(decimal(t,"amount"))}), BorderLayout.CENTER);
        lower.add(chart); lower.add(recent); body.add(cards, BorderLayout.NORTH); body.add(lower, BorderLayout.CENTER); shell.add(body, BorderLayout.CENTER); return shell;
    }

    private JPanel transactionsPage() {
        JPanel shell = pageShell("Financial activity", "Transactions", "Search and manage income and expenses in one place.");
        JPanel body = new JPanel(new BorderLayout(0, 12)); body.setOpaque(false); JPanel actions = new JPanel(new BorderLayout()); actions.setOpaque(false);
        JTextField search = field("Filter by description or source"); actions.add(search, BorderLayout.CENTER); JButton add = primaryButton("＋  Add transaction"); JButton delete = flatButton("Delete selected"); JPanel controls = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0)); controls.setOpaque(false); controls.add(delete); controls.add(add); actions.add(controls, BorderLayout.EAST);
        DefaultTableModel model = model(new String[]{"ID","DATE","TYPE","DESCRIPTION","CATEGORY","PAYMENT","AMOUNT"}); JTable grid = table(model); grid.removeColumn(grid.getColumnModel().getColumn(0));
        Runnable fill = () -> { model.setRowCount(0); String term = search.getText().toLowerCase(Locale.ROOT); transactions.stream().filter(t -> (t.path("description").asText("")+" "+t.path("source").asText("")+" "+categoryName(t)).toLowerCase(Locale.ROOT).contains(term)).forEach(t -> model.addRow(new Object[]{t.path("id").asText(), date(t), t.path("transaction_type").asText(), t.path("description").asText(t.path("source").asText("—")), categoryName(t), t.path("payment_method").asText("—"), money(decimal(t,"amount"))})); };
        fill.run(); search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){fill.run();}public void removeUpdate(javax.swing.event.DocumentEvent e){fill.run();}public void changedUpdate(javax.swing.event.DocumentEvent e){fill.run();}});
        add.addActionListener(e -> transactionDialog(null));
        delete.addActionListener(e -> {int row=grid.getSelectedRow();if(row<0){error("Select a transaction to delete.");return;}String id=String.valueOf(model.getValueAt(grid.convertRowIndexToModel(row),0));if(JOptionPane.showConfirmDialog(frame,"Delete this transaction?","Confirm delete",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)runTask(()->api.delete("transactions",id),()->refreshData(()->showPage("Transactions")),ex->error(ex.getMessage()));});
        body.add(actions, BorderLayout.NORTH); body.add(card(new JScrollPane(grid)), BorderLayout.CENTER); shell.add(body, BorderLayout.CENTER); return shell;
    }

    private void transactionDialog(JsonNode existing) {
        JComboBox<String> type = new JComboBox<>(new String[]{"EXPENSE","INCOME"}); JTextField amount = field("0.00"); JTextField date = field(LocalDate.now().toString()); JTextField description = field("Optional note"); JTextField source = field("Optional source");
        JComboBox<JsonNode> category = new JComboBox<>(); JComboBox<String> payment = new JComboBox<>(new String[]{"CASH","UPI","DEBIT_CARD","CREDIT_CARD","NET_BANKING","OTHER"});
        Runnable filter = () -> { category.removeAllItems(); categories.stream().filter(c -> c.path("type").asText().equals(type.getSelectedItem())).forEach(category::addItem); };
        category.setRenderer((list, value, index, selected, focus) -> { JLabel label = new JLabel(value == null ? "Select a category" : value.path("name").asText()); label.setOpaque(true); label.setBorder(BorderFactory.createEmptyBorder(5,8,5,8)); label.setBackground(selected ? new Color(226,241,232) : WHITE); label.setForeground(INK); return label; });
        type.addActionListener(e -> filter.run()); filter.run();
        JPanel p = formPanel(new String[][]{{"Type",""},{"Amount",""},{"Date (YYYY-MM-DD)",""},{"Category",""},{"Payment method",""},{"Description",""},{"Source",""}});
        setFormComponent(p,0,type);setFormComponent(p,1,amount);setFormComponent(p,2,date);setFormComponent(p,3,category);setFormComponent(p,4,payment);setFormComponent(p,5,description);setFormComponent(p,6,source);
        if(existing!=null){type.setSelectedItem(existing.path("transaction_type").asText());amount.setText(existing.path("amount").asText());date.setText(date(existing));description.setText(existing.path("description").asText());source.setText(existing.path("source").asText());payment.setSelectedItem(existing.path("payment_method").asText("OTHER"));}
        int choice=JOptionPane.showConfirmDialog(frame,p,"New transaction",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(choice!=JOptionPane.OK_OPTION)return;
        JsonNode selected=(JsonNode)category.getSelectedItem(); if(selected==null){error("Add or select a category first.");return;}
        try { BigDecimal value=new BigDecimal(amount.getText().trim()); if(value.signum()<=0)throw new NumberFormatException(); LocalDate.parse(date.getText().trim());
            ObjectNode row=json.createObjectNode().put("user_id",user.path("id").asText()).put("category_id",selected.path("id").asLong()).put("transaction_type",String.valueOf(type.getSelectedItem())).put("amount",value).put("transaction_date",date.getText().trim()).put("payment_method",String.valueOf(payment.getSelectedItem())); nullable(row,"description",description.getText());nullable(row,"source",source.getText());
            runTask(()->api.insert("transactions",row),()->refreshData(()->showPage("Transactions")),ex->error(ex.getMessage()));
        } catch(Exception ex){error("Enter a positive amount and a valid date (YYYY-MM-DD).");}
    }

    private JPanel categoriesPage() {
        JPanel shell=pageShell("Manage labels","Categories","Organize income and expense transactions."); JPanel body=new JPanel(new BorderLayout(0,12));body.setOpaque(false);
        JPanel actions=new JPanel(new BorderLayout());actions.setOpaque(false);JLabel count=new JLabel(categories.size()+" categories available");count.setForeground(MUTED);JButton add=primaryButton("＋  Add category");actions.add(count,BorderLayout.WEST);actions.add(add,BorderLayout.EAST);
        DefaultTableModel m=model(new String[]{"NAME","TYPE","COLOR","SCOPE"});categories.forEach(c->m.addRow(new Object[]{c.path("name").asText(),c.path("type").asText(),c.path("color").asText(),c.path("user_id").isNull()?"Shared default":"Personal"}));
        JTable grid=table(m);add.addActionListener(e->{JTextField name=field("");JComboBox<String> type=new JComboBox<>(new String[]{"EXPENSE","INCOME"});JPanel p=formPanel(new String[][]{{"Name",""},{"Type",""}});setFormComponent(p,0,name);setFormComponent(p,1,type);if(JOptionPane.showConfirmDialog(frame,p,"Add category",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION&&!name.getText().isBlank()){ObjectNode row=json.createObjectNode().put("user_id",user.path("id").asText()).put("name",name.getText().trim()).put("type",String.valueOf(type.getSelectedItem())).put("icon","circle").put("color","#3978e8");runTask(()->api.insert("categories",row),()->refreshData(()->showPage("Categories")),ex->error(ex.getMessage()));}});
        body.add(actions,BorderLayout.NORTH);body.add(card(new JScrollPane(grid)),BorderLayout.CENTER);shell.add(body,BorderLayout.CENTER);return shell;
    }

    private JPanel budgetPage() {
        JPanel shell=pageShell("Monthly plan","Budget planner","Set an overall monthly limit and track actual spending."); JPanel body=new JPanel(new BorderLayout(0,14));body.setOpaque(false);
        JPanel form=card();form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));form.add(sectionTitle("Monthly budget","One budget per month"));JPanel row=new JPanel(new GridLayout(1,3,12,0));row.setOpaque(false);
        JComboBox<String> month=new JComboBox<>(new String[]{"01","02","03","04","05","06","07","08","09","10","11","12"});month.setSelectedItem(String.format("%02d",LocalDate.now().getMonthValue()));JTextField year=field(String.valueOf(LocalDate.now().getYear()));JTextField total=field("");row.add(labeled("Month",month));row.add(labeled("Year",year));row.add(labeled("Overall monthly budget",total));form.add(row);form.add(Box.createVerticalStrut(12));JButton save=primaryButton("Save monthly budget");form.add(save);save.addActionListener(e->{try{int y=Integer.parseInt(year.getText().trim()),m=Integer.parseInt(String.valueOf(month.getSelectedItem()));BigDecimal b=new BigDecimal(total.getText().trim());if(b.signum()<=0)throw new NumberFormatException();ObjectNode plan=json.createObjectNode().put("user_id",user.path("id").asText()).put("budget_month",m).put("budget_year",y).put("total_budget",b);runTask(()->api.upsertBudget(plan),()->refreshData(()->showPage("Budget planner")),ex->error(ex.getMessage()));}catch(Exception ex){error("Enter a valid year and positive budget amount.");}});
        DefaultTableModel m=model(new String[]{"PERIOD","PLANNED","ACTUAL SPENDING","REMAINING","STATUS"});for(JsonNode b:budgets){int y=b.path("budget_year").asInt(),mo=b.path("budget_month").asInt();BigDecimal actual=transactions.stream().filter(t->t.path("transaction_type").asText().equals("EXPENSE")&&date(t).startsWith(String.format("%04d-%02d",y,mo))).map(t->decimal(t,"amount")).reduce(BigDecimal.ZERO,BigDecimal::add);BigDecimal planned=decimal(b,"total_budget");m.addRow(new Object[]{YearMonth.of(y,mo).format(DateTimeFormatter.ofPattern("MMMM yyyy")),money(planned),money(actual),money(planned.subtract(actual)),actual.compareTo(planned)>=0?"Over budget":actual.compareTo(planned.multiply(new BigDecimal("0.8")))>=0?"Near limit":"On track"});}
        JPanel history=card(new JScrollPane(table(m)));body.add(form,BorderLayout.NORTH);body.add(history,BorderLayout.CENTER);shell.add(body,BorderLayout.CENTER);return shell;
    }

    private JPanel goalsPage() {
        JPanel shell=pageShell("Plan & grow","Savings goals","Keep your goals visible and follow progress over time."); JPanel body=new JPanel(new BorderLayout(0,12));body.setOpaque(false);JPanel bar=new JPanel(new BorderLayout());bar.setOpaque(false);bar.add(new JLabel(goals.size()+" goals"),BorderLayout.WEST);JButton add=primaryButton("＋  Add savings goal");bar.add(add,BorderLayout.EAST);
        DefaultTableModel m=model(new String[]{"GOAL","TARGET","SAVED","REMAINING","STATUS"});for(JsonNode g:goals){BigDecimal target=decimal(g,"target_amount");BigDecimal contributed=BigDecimal.ZERO;JsonNode cs=g.path("savings_contributions");if(cs.isArray())for(JsonNode c:cs)contributed=contributed.add(decimal(c,"amount"));m.addRow(new Object[]{g.path("name").asText(),money(target),money(contributed),money(target.subtract(contributed)),g.path("status").asText()});}
        add.addActionListener(e->{JTextField name=field("");JTextField target=field("");JTextField date=field("");JTextArea desc=new JTextArea(3,20);JPanel p=formPanel(new String[][]{{"Goal name",""},{"Target amount",""},{"Target date (YYYY-MM-DD, optional)",""},{"Description",""}});setFormComponent(p,0,name);setFormComponent(p,1,target);setFormComponent(p,2,date);setFormComponent(p,3,new JScrollPane(desc));if(JOptionPane.showConfirmDialog(frame,p,"Create savings goal",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)try{BigDecimal amount=new BigDecimal(target.getText().trim());ObjectNode row=json.createObjectNode().put("user_id",user.path("id").asText()).put("name",name.getText().trim()).put("target_amount",amount).put("status","ACTIVE");if(!date.getText().isBlank())row.put("target_date",LocalDate.parse(date.getText().trim()).toString());nullable(row,"description",desc.getText());runTask(()->api.insert("savings_goals",row),()->refreshData(()->showPage("Savings goals")),ex->error(ex.getMessage()));}catch(Exception ex){error("Enter a valid target amount and date.");}});
        body.add(bar,BorderLayout.NORTH);body.add(card(new JScrollPane(table(m))),BorderLayout.CENTER);shell.add(body,BorderLayout.CENTER);return shell;
    }

    private JPanel interestPage() {
        JPanel shell=pageShell("Estimate & compare","Interest calculator","Explore simple and compound growth. Results are estimates.");JPanel body=new JPanel(new BorderLayout(0,14));body.setOpaque(false);JPanel form=card();form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));form.add(sectionTitle("Investment assumptions","Calculate simple or compound interest"));
        JPanel row=new JPanel(new GridLayout(1,4,12,0));row.setOpaque(false);JTextField principal=field("");JTextField rate=field("");JTextField duration=field("");JComboBox<String> type=new JComboBox<>(new String[]{"COMPOUND","SIMPLE"});row.add(labeled("Principal",principal));row.add(labeled("Annual rate (%)",rate));row.add(labeled("Duration (years)",duration));row.add(labeled("Calculation",type));form.add(row);form.add(Box.createVerticalStrut(12));JLabel result=new JLabel("Enter assumptions to see an estimate.");result.setFont(BOLD);result.setForeground(GREEN);JButton calculate=primaryButton("Calculate and save");form.add(calculate);form.add(Box.createVerticalStrut(12));form.add(result);
        calculate.addActionListener(e->{try{BigDecimal p=new BigDecimal(principal.getText().trim()),r=new BigDecimal(rate.getText().trim()),y=new BigDecimal(duration.getText().trim());if(p.signum()<=0||r.signum()<0||y.signum()<=0)throw new NumberFormatException();boolean compound=type.getSelectedItem().equals("COMPOUND");BigDecimal finalValue=compound?p.multiply(BigDecimal.valueOf(Math.pow(1+r.divide(new BigDecimal("100"),12,RoundingMode.HALF_UP).doubleValue(),y.doubleValue()))):p.multiply(BigDecimal.ONE.add(r.multiply(y).divide(new BigDecimal("100"),10,RoundingMode.HALF_UP)));BigDecimal interest=finalValue.subtract(p).setScale(2,RoundingMode.HALF_UP);ObjectNode rowValue=json.createObjectNode().put("user_id",user.path("id").asText()).put("calculation_type",compound?"COMPOUND":"SIMPLE").put("principal",p).put("annual_rate",r).put("duration",y).put("duration_unit","YEARS").put("compounding_frequency",compound?"YEARLY":(String)null).put("interest_amount",interest).put("final_amount",finalValue.setScale(2,RoundingMode.HALF_UP));result.setText("Estimated interest: "+money(interest)+"    ·    Projected total: "+money(finalValue));runTask(()->api.insert("interest_calculations",rowValue),()->statusLabel.setText("Calculation saved to your account"),ex->error(ex.getMessage()));}catch(Exception ex){error("Enter a positive principal, non-negative rate, and valid duration.");}});
        DefaultTableModel hist=model(new String[]{"TYPE","PRINCIPAL","RATE","DURATION","INTEREST","FINAL AMOUNT"});try{for(JsonNode c:api.interestHistory())hist.addRow(new Object[]{c.path("calculation_type").asText(),money(decimal(c,"principal")),c.path("annual_rate").asText()+"%",c.path("duration").asText()+" "+c.path("duration_unit").asText().toLowerCase(),money(decimal(c,"interest_amount")),money(decimal(c,"final_amount"))});}catch(Exception e){statusLabel.setText("Interest history unavailable");}
        body.add(form,BorderLayout.NORTH);body.add(card(new JScrollPane(table(hist))),BorderLayout.CENTER);shell.add(body,BorderLayout.CENTER);return shell;
    }

    private JPanel reportsPage() {
        JPanel shell=pageShell("Your financial records","Reports & analytics","Review the current month and export your transactions as CSV.");JPanel body=new JPanel(new BorderLayout(0,12));body.setOpaque(false);JPanel metrics=new JPanel(new GridLayout(1,4,12,0));metrics.setOpaque(false);YearMonth month=YearMonth.now();List<JsonNode> current=transactions.stream().filter(t->date(t).startsWith(month.toString())).toList();BigDecimal in=sum(current,"INCOME"),out=sum(current,"EXPENSE");metrics.add(metric("Income",money(in),month.toString()));metrics.add(metric("Expenses",money(out),month.toString()));metrics.add(metric("Net",money(in.subtract(out)),"Income less expenses"));metrics.add(metric("Transactions",String.valueOf(current.size()),"This month"));
        DefaultTableModel m=model(new String[]{"DATE","DESCRIPTION","CATEGORY","TYPE","AMOUNT"});for(JsonNode t:current)m.addRow(new Object[]{date(t),t.path("description").asText(t.path("source").asText("—")),categoryName(t),t.path("transaction_type").asText(),money(decimal(t,"amount"))});JButton export=primaryButton("Export CSV");export.addActionListener(e->exportCsv(m));JPanel top=new JPanel(new BorderLayout());top.setOpaque(false);top.add(metrics,BorderLayout.CENTER);top.add(export,BorderLayout.EAST);body.add(top,BorderLayout.NORTH);body.add(card(new JScrollPane(table(m))),BorderLayout.CENTER);shell.add(body,BorderLayout.CENTER);return shell;
    }

    private JPanel profilePage() {
        JPanel shell=pageShell("Preferences","Profile & settings","Account details and secure access.");JPanel info=card();info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));info.add(sectionTitle("Account","Connected Supabase account"));info.add(label("Name: "+user.path("user_metadata").path("full_name").asText("—")));info.add(Box.createVerticalStrut(8));info.add(label("Email: "+user.path("email").asText("—")));info.add(Box.createVerticalStrut(8));info.add(label("User ID: "+user.path("id").asText("—")));info.add(Box.createVerticalStrut(22));JButton signOut=flatButton("Sign out");signOut.setAlignmentX(Component.LEFT_ALIGNMENT);signOut.addActionListener(e->{api.signOut();user=null;showLogin();});info.add(signOut);shell.add(info,BorderLayout.CENTER);return shell;
    }

    private JPanel metric(String title, String value, String foot) {
        JPanel p=card();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));JLabel name=new JLabel(title);name.setForeground(MUTED);name.setFont(BODY);JLabel number=new JLabel(value);number.setForeground(INK);number.setFont(new Font("Segoe UI",Font.BOLD,23));JLabel caption=new JLabel(foot);caption.setForeground(MUTED);caption.setFont(new Font("Segoe UI",Font.PLAIN,11));p.add(name);p.add(Box.createVerticalStrut(14));p.add(number);p.add(Box.createVerticalStrut(6));p.add(caption);return p;
    }

    private JPanel sectionTitle(String title,String subtitle){JPanel p=new JPanel();p.setOpaque(false);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));JLabel t=new JLabel(title);t.setFont(BOLD);t.setForeground(INK);JLabel s=new JLabel(subtitle);s.setFont(new Font("Segoe UI",Font.PLAIN,12));s.setForeground(MUTED);p.add(t);p.add(Box.createVerticalStrut(3));p.add(s);return p;}
    private JPanel card(){JPanel p=new JPanel();p.setBackground(WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),BorderFactory.createEmptyBorder(18,18,18,18)));return p;}
    private JPanel card(Component child){JPanel p=card();p.setLayout(new BorderLayout());p.add(child,BorderLayout.CENTER);return p;}
    private JPanel labeled(String title,Component field){JPanel p=new JPanel();p.setOpaque(false);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));JLabel l=label(title);p.add(l);p.add(Box.createVerticalStrut(5));field.setPreferredSize(new Dimension(150,40));field.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));p.add(field);return p;}
    private JLabel label(String text){JLabel l=new JLabel(text);l.setFont(BODY);l.setForeground(MUTED);return l;}
    private JTextField field(String text){JTextField f=new JTextField(text);styleField(f);return f;}
    private void styleField(JTextField f){f.setFont(BODY);f.setForeground(INK);f.setBackground(WHITE);f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),BorderFactory.createEmptyBorder(9,11,9,11)));f.setPreferredSize(new Dimension(360,44));f.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));f.setAlignmentX(Component.LEFT_ALIGNMENT);}
    private JButton primaryButton(String title){JButton b=new JButton(title);b.setFont(BOLD);b.setForeground(WHITE);b.setBackground(GREEN);b.setFocusPainted(false);b.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));return b;}
    private JButton flatButton(String title){JButton b=new JButton(title);b.setFont(BODY);b.setForeground(INK);b.setBackground(WHITE);b.setFocusPainted(false);b.setBorder(BorderFactory.createEmptyBorder(9,12,9,12));b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));return b;}
    private DefaultTableModel model(String[] columns){return new DefaultTableModel(columns,0){@Override public boolean isCellEditable(int row,int column){return false;}};}
    private JTable table(String[] cols,List<JsonNode> rows,java.util.function.Function<JsonNode,Object[]> mapper){DefaultTableModel m=model(cols);rows.forEach(r->m.addRow(mapper.apply(r)));return table(m);}
    private JTable table(DefaultTableModel model){JTable t=new JTable(model);t.setFont(BODY);t.setRowHeight(34);t.setForeground(INK);t.setBackground(WHITE);t.setGridColor(new Color(239,242,239));t.setFillsViewportHeight(true);t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,11));t.getTableHeader().setForeground(MUTED);t.getTableHeader().setBackground(new Color(248,250,248));t.getTableHeader().setPreferredSize(new Dimension(0,36));t.setSelectionBackground(new Color(231,243,235));t.setSelectionForeground(INK);return t;}
    private JPanel formPanel(String[][] fields){JPanel p=new JPanel(new GridLayout(fields.length,2,10,10));p.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));for(String[] f:fields){p.add(label(f[0]));p.add(new JTextField());}return p;}
    private void setFormComponent(JPanel p,int row,Component c){p.remove(row*2+1);p.add(c,row*2+1);}
    private void nullable(ObjectNode node,String key,String value){if(value==null||value.isBlank())node.putNull(key);else node.put(key,value.trim());}
    private String date(JsonNode t){return t.path("transaction_date").asText(t.path("date").asText(""));}
    private String categoryName(JsonNode t){JsonNode joined=t.path("categories");if(joined.isArray()&&!joined.isEmpty())joined=joined.get(0);return joined.path("name").asText(t.path("category").asText("Unknown"));}
    private BigDecimal decimal(JsonNode n,String key){try{return new BigDecimal(n.path(key).asText("0"));}catch(Exception e){return BigDecimal.ZERO;}}
    private BigDecimal sum(List<JsonNode> rows,String type){return rows.stream().filter(t->t.path("transaction_type").asText().equals(type)).map(t->decimal(t,"amount")).reduce(BigDecimal.ZERO,BigDecimal::add);}
    private BigDecimal allTime(String type){return sum(transactions,type);}
    private String money(BigDecimal amount){String currency=user==null?"USD":user.path("user_metadata").path("currency").asText("USD");return currency+" "+amount.setScale(2,RoundingMode.HALF_UP).toPlainString();}
    private void error(String message){JOptionPane.showMessageDialog(frame,message,"Ledgerly",JOptionPane.ERROR_MESSAGE);}
    private void setStatus(String message){statusLabel.setText(message);}

    private void refreshData(Runnable done){setStatus("Syncing with Supabase…");runTask(()->{categories=api.categories();transactions=api.transactions();budgets=api.budgets();goals=api.goals();},()->{setStatus("Connected to Supabase");if(done!=null)done.run();},ex->{setStatus("Connection issue");error(ex.getMessage());});}
    private void runTask(ThrowingRunnable work,Runnable done,java.util.function.Consumer<Exception> failure){new SwingWorker<Void,Void>(){private Exception error;@Override protected Void doInBackground(){try{work.run();}catch(Exception e){error=e;}return null;}@Override protected void done(){if(error==null){if(done!=null)done.run();}else failure.accept(error);}}.execute();}

    private void exportCsv(DefaultTableModel model){JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(Path.of("ledgerly-report.csv").toFile());if(chooser.showSaveDialog(frame)!=JFileChooser.APPROVE_OPTION)return;try{StringBuilder csv=new StringBuilder();for(int r=-1;r<model.getRowCount();r++){int count=model.getColumnCount();for(int c=0;c<count;c++){if(c>0)csv.append(',');String v=r<0?String.valueOf(model.getColumnName(c)):String.valueOf(model.getValueAt(r,c));csv.append('"').append(v.replace("\"","\"\"")).append('"');}csv.append("\r\n");}Files.writeString(chooser.getSelectedFile().toPath(),csv.toString(),StandardCharsets.UTF_8);setStatus("Report exported");}catch(IOException e){error("Could not write the CSV file: "+e.getMessage());}}

    private final class BarChart extends JPanel {
        private final double income, expense;
        private BarChart(double income,double expense){this.income=income;this.expense=expense;setBackground(WHITE);setPreferredSize(new Dimension(350,210));}
        @Override protected void paintComponent(Graphics graphics){super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int base=getHeight()-38;double max=Math.max(1,Math.max(income,expense));int h1=(int)((getHeight()-80)*income/max),h2=(int)((getHeight()-80)*expense/max);g.setColor(new Color(62,154,113));g.fillRoundRect(getWidth()/2-75,base-h1,54,h1,12,12);g.setColor(new Color(218,113,117));g.fillRoundRect(getWidth()/2+20,base-h2,54,h2,12,12);g.setColor(MUTED);g.setFont(BODY);g.drawString("Income",getWidth()/2-82,base+22);g.drawString("Expenses",getWidth()/2+10,base+22);g.dispose();}
    }

    @FunctionalInterface private interface ThrowingRunnable { void run() throws Exception; }

    private static final class SupabaseClient {
        private final String url, key; private final ObjectMapper mapper=new ObjectMapper(); private final HttpClient http=HttpClient.newBuilder().build(); private String accessToken;
        private SupabaseClient(String url,String key){this.url=url.replaceAll("/$","");this.key=key;}
        static SupabaseClient fromProjectConfig(){String url=System.getenv().getOrDefault("SUPABASE_URL","");String key=System.getenv().getOrDefault("SUPABASE_ANON_KEY","");if(url.isBlank()||key.isBlank()){for(Path p:List.of(Path.of("frontend/assets/js/config.js"),Path.of("outputs/personal-expense-tracker/frontend/assets/js/config.js"))){try{String text=Files.readString(p);if(url.isBlank())url=extract(text,"supabaseUrl");if(key.isBlank())key=extract(text,"supabaseAnonKey");if(!url.isBlank()&&!key.isBlank())break;}catch(IOException ignored){}}}if(url.isBlank())url="https://odjmwosqswnetfsgvydv.supabase.co";return new SupabaseClient(url,key);}
        private static String extract(String text,String key){Matcher m=Pattern.compile(key+"\\s*:\\s*['\"]([^'\"]+)['\"]").matcher(text);return m.find()?m.group(1):"";}
        JsonNode signIn(String email,String password)throws Exception{ensureKey();JsonNode result=send("POST","/auth/v1/token?grant_type=password",mapper.createObjectNode().put("email",email).put("password",password),false);accessToken=result.path("access_token").asText("");if(accessToken.isBlank())throw new IOException("Supabase did not return a sign-in session. Check email confirmation settings.");return result.path("user");}
        JsonNode register(String name,String email,String password,String currency)throws Exception{ensureKey();ObjectNode body=mapper.createObjectNode().put("email",email).put("password",password);body.putObject("data").put("full_name",name).put("currency",currency);JsonNode result=send("POST","/auth/v1/signup",body,false);accessToken=result.path("access_token").asText("");if(accessToken.isBlank())throw new IOException("Account created. Confirm your email, then sign in.");return result.path("user");}
        void signOut(){if(accessToken!=null)try{send("POST","/auth/v1/logout",mapper.createObjectNode(),true);}catch(Exception ignored){}accessToken=null;}
        List<JsonNode> categories()throws Exception{return list("/rest/v1/categories?select=id,user_id,name,type,icon,color&order=name.asc");}
        List<JsonNode> transactions()throws Exception{return list("/rest/v1/transactions?select=id,category_id,transaction_type,amount,transaction_date,source,payment_method,description,categories(name,color)&order=transaction_date.desc&limit=1000");}
        List<JsonNode> budgets()throws Exception{return list("/rest/v1/budget_plans?select=id,budget_month,budget_year,total_budget&order=budget_year.desc,budget_month.desc&limit=120");}
        List<JsonNode> goals()throws Exception{return list("/rest/v1/savings_goals?select=id,name,target_amount,target_date,status,savings_contributions(amount)&order=created_at.desc");}
        List<JsonNode> interestHistory()throws Exception{return list("/rest/v1/interest_calculations?select=id,calculation_type,principal,annual_rate,duration,duration_unit,interest_amount,final_amount,created_at&order=created_at.desc&limit=30");}
        void insert(String table,JsonNode row)throws Exception{send("POST","/rest/v1/"+table,row,true);}
        void delete(String table,String id)throws Exception{send("DELETE","/rest/v1/"+table+"?id=eq."+encode(id),null,true);}
        void upsertBudget(JsonNode row)throws Exception{send("POST","/rest/v1/budget_plans?on_conflict=user_id,budget_year,budget_month",row,true,"resolution=merge-duplicates,return=representation");}
        private List<JsonNode> list(String path)throws Exception{JsonNode n=send("GET",path,null,true);List<JsonNode> rows=new ArrayList<>();if(n!=null&&n.isArray())n.forEach(rows::add);return rows;}
        private JsonNode send(String method,String path,JsonNode body,boolean auth)throws Exception{return send(method,path,body,auth,null);}
        private JsonNode send(String method,String path,JsonNode body,boolean auth,String prefer)throws Exception{ensureKey();HttpRequest.Builder b=HttpRequest.newBuilder(URI.create(url+path)).header("apikey",key).header("Accept","application/json");if(auth){if(accessToken==null||accessToken.isBlank())throw new IOException("Sign in to access your private financial records.");b.header("Authorization","Bearer "+accessToken);}else b.header("Authorization","Bearer "+key);if(prefer!=null)b.header("Prefer",prefer);if(body!=null)b.header("Content-Type","application/json");HttpRequest req=b.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();HttpResponse<String> res=http.send(req,HttpResponse.BodyHandlers.ofString());if(res.statusCode()<200||res.statusCode()>=300){String message=res.body();try{JsonNode error=mapper.readTree(message);message=error.path("msg").asText(error.path("message").asText(error.path("error_description").asText(message)));}catch(Exception ignored){}throw new IOException(message.isBlank()?"Supabase request failed ("+res.statusCode()+")":message);}if(res.body()==null||res.body().isBlank())return null;return mapper.readTree(res.body());}
        private void ensureKey()throws IOException{if(key==null||key.isBlank())throw new IOException("Supabase publishable key is missing. Set SUPABASE_ANON_KEY or configure frontend/assets/js/config.js.");}
        private static String encode(String v){return URLEncoder.encode(v,StandardCharsets.UTF_8);}
    }
}
