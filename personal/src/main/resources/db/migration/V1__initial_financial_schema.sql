CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(254) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'USD',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE categories (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NULL,
  name VARCHAR(80) NOT NULL,
  type VARCHAR(8) NOT NULL,
  icon VARCHAR(40) NOT NULL DEFAULT 'circle',
  color CHAR(7) NOT NULL DEFAULT '#3478F6',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT uq_categories_user_name_type UNIQUE (user_id, name, type),
  CONSTRAINT chk_categories_type CHECK (type IN ('INCOME','EXPENSE')),
  INDEX ix_categories_type (type)
);

CREATE TABLE transactions (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  transaction_type VARCHAR(8) NOT NULL,
  amount DECIMAL(19,2) NOT NULL,
  transaction_date DATE NOT NULL,
  source VARCHAR(100) NULL,
  payment_method VARCHAR(20) NULL,
  description VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_transactions_amount CHECK (amount > 0),
  CONSTRAINT chk_transactions_type CHECK (transaction_type IN ('INCOME','EXPENSE')),
  CONSTRAINT chk_transactions_payment CHECK (payment_method IS NULL OR payment_method IN ('CASH','UPI','DEBIT_CARD','CREDIT_CARD','NET_BANKING','OTHER')),
  CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories(id),
  INDEX ix_transactions_user_date (user_id, transaction_date),
  INDEX ix_transactions_user_type_date (user_id, transaction_type, transaction_date),
  INDEX ix_transactions_category (category_id)
);

CREATE TABLE budget_plans (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  budget_month INT NOT NULL,
  budget_year INT NOT NULL,
  total_budget DECIMAL(19,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_budget_month CHECK (budget_month BETWEEN 1 AND 12),
  CONSTRAINT chk_budget_total CHECK (total_budget > 0),
  CONSTRAINT uq_budget_user_month UNIQUE (user_id, budget_year, budget_month),
  CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE budget_allocations (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  budget_plan_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  planned_amount DECIMAL(19,2) NOT NULL,
  CONSTRAINT chk_allocation_amount CHECK (planned_amount > 0),
  CONSTRAINT uq_allocation_category UNIQUE (budget_plan_id, category_id),
  CONSTRAINT fk_allocation_budget FOREIGN KEY (budget_plan_id) REFERENCES budget_plans(id) ON DELETE CASCADE,
  CONSTRAINT fk_allocation_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE savings_goals (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  target_amount DECIMAL(19,2) NOT NULL,
  target_date DATE NULL,
  description VARCHAR(500) NULL,
  status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_goal_target CHECK (target_amount > 0),
  CONSTRAINT chk_goal_status CHECK (status IN ('ACTIVE','COMPLETED','ARCHIVED')),
  CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX ix_goals_user_status (user_id, status)
);

CREATE TABLE savings_contributions (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  savings_goal_id BIGINT NOT NULL,
  amount DECIMAL(19,2) NOT NULL,
  contribution_date DATE NOT NULL,
  description VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_contribution_amount CHECK (amount > 0),
  CONSTRAINT fk_contribution_goal FOREIGN KEY (savings_goal_id) REFERENCES savings_goals(id) ON DELETE CASCADE,
  INDEX ix_contributions_goal_date (savings_goal_id, contribution_date)
);

CREATE TABLE interest_calculations (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  calculation_type VARCHAR(10) NOT NULL,
  principal DECIMAL(19,2) NOT NULL,
  annual_rate DECIMAL(9,4) NOT NULL,
  duration DECIMAL(9,4) NOT NULL,
  duration_unit VARCHAR(10) NOT NULL,
  compounding_frequency VARCHAR(15) NULL,
  interest_amount DECIMAL(19,2) NOT NULL,
  final_amount DECIMAL(19,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_interest_type CHECK (calculation_type IN ('SIMPLE','COMPOUND')),
  CONSTRAINT chk_interest_duration_unit CHECK (duration_unit IN ('YEARS','MONTHS')),
  CONSTRAINT chk_interest_frequency CHECK (compounding_frequency IS NULL OR compounding_frequency IN ('YEARLY','HALF_YEARLY','QUARTERLY','MONTHLY')),
  CONSTRAINT fk_interest_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX ix_interest_user_created (user_id, created_at)
);
