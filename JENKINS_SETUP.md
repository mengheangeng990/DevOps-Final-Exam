# Jenkins Setup Guide for Windows

## Prerequisites
- Java 11 or higher installed
- Git installed
- Maven installed
- Ansible installed (in WSL or native)
- Docker installed (for MySQL if needed)

---

## Step 1: Download Jenkins

1. Go to https://jenkins.io/download/
2. Download **Jenkins for Windows** (jenkins.msi)
3. Or download **jenkins.war** for portable use

---

## Step 2: Install Jenkins (Windows MSI)

1. Run the downloaded `jenkins.msi` installer
2. Follow the installation wizard
3. When prompted, configure:
   - **Port**: `8080` (default)
   - **Service Account**: Use Windows user account
4. Complete installation
5. Jenkins will start automatically as a Windows Service

**Alternative: Run Jenkins as WAR file**
```cmd
cd C:\path\to\jenkins
java -jar jenkins.war --httpPort=8080
```

---

## Step 3: Initial Jenkins Setup

1. Open browser: `http://localhost:8080`
2. Copy the initial admin password from:
   - Windows Service: `C:\Program Files\Jenkins\secrets\initialAdminPassword`
   - Or from console output if running as WAR
3. Paste the password and click **Continue**
4. Click **Install suggested plugins**
5. Create first admin user
6. Configure Jenkins URL (use `http://localhost:8080`)

---

## Step 4: Install Required Plugins

1. Go to **Manage Jenkins** → **Manage Plugins**
2. Go to **Available** tab
3. Search and install these plugins:
   - **Pipeline** (Declarative Pipeline)
   - **Git** (Git plugin)
   - **Email Extension Plugin** (for email notifications)
   - **AnsiColor** (for colored logs)
   - **Log Parser Plugin** (optional)

4. Click **Download now and install after restart**
5. Wait for plugins to install and Jenkins to restart

---

## Step 5: Configure Email Settings

1. Go to **Manage Jenkins** → **System Configuration**
2. Scroll to **Extended E-mail Notification**
3. Configure:

### Option A: Gmail SMTP
```
SMTP server: smtp.gmail.com
SMTP port: 587
Use SSL: ✓ (checked)
Use TLS: ✓ (checked)
```

**Credentials Setup:**
- Username: `your-email@gmail.com`
- Password: Generate App Password (not regular Gmail password)
  1. Go to https://myaccount.google.com/apppasswords
  2. Select Mail and Windows
  3. Copy the generated 16-char password
  4. Use this as Jenkins password

### Option B: Outlook/Corporate Email
```
SMTP server: smtp.office365.com
SMTP port: 587
Use TLS: ✓
```

4. Click **Apply** and **Save**

---

## Step 6: Configure Git Credentials

1. Go to **Manage Jenkins** → **Manage Credentials**
2. Click **System** → **Global credentials**
3. Click **Add Credentials**
4. Select **Username with password** (for GitHub personal token)
   - Username: Your GitHub username
   - Password: GitHub Personal Access Token
   - Description: `GitHub PAT`
5. Click **Create**

---

## Step 7: Create Pipeline Job

1. Click **New Item**
2. Enter Job Name: `IDCard-Manager-Pipeline`
3. Select **Pipeline**
4. Click **OK**

### Configure General
- **Description**: `Auto-build and deploy ID Card Manager`
- **Discard old builds**: 
  - Days to keep builds: `7`
  - Max builds to keep: `10`

### Configure Pipeline
- **Definition**: `Pipeline script from SCM`
- **SCM**: `Git`
  - **Repository URL**: `https://github.com/mengheangeng990/DevOps-Final-Exam.git`
  - **Credentials**: Select your GitHub credentials
  - **Branch**: `*/main`
  - **Script Path**: `Jenkinsfile`

### Advanced Options
```
Lightweight checkout: ✓ (checked)
Poll SCM: ✓ (checked)
  - Schedule: */5 * * * *  (every 5 minutes)
```

5. Click **Apply** and **Save**

---

## Step 8: Run First Build

1. Go back to the job page
2. Click **Build Now**
3. Watch the build in real-time:
   - Click **#1** (first build)
   - Click **Console Output**
4. Monitor the stages:
   - ✅ Checkout
   - ✅ Clean
   - ✅ Build
   - ✅ Test
   - ✅ Deploy

---

## Step 9: Verify Email Notifications

### Test Email Configuration
1. Go to **Manage Jenkins** → **System Configuration**
2. Scroll to **Extended E-mail Notification**
3. Find **Default Recipient List**: `srengty@gmail.com`
4. Click **Test Configuration** button
5. Check your email for test message

---

## Step 10: Configure Git Commit Email Notifications

The Jenkinsfile automatically sends emails to:
- **srengty@gmail.com** (main recipient)
- **Commit author** (developer who made the change)
- **Build requestor**
- **Broken build suspects** (on failure)

### Configure Jenkins to Extract Committer Email
1. Go to job configuration
2. Under **General**, check:
   - **GitHub project** (if using GitHub)
   - Enter repository URL

---

## Jenkinsfile Workflow

```
┌──────────────────────────────────────┐
│   Poll SCM every 5 minutes           │
│   (Check Git for updates)            │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│   Stage: Checkout                    │
│   (Clone/pull from Git)              │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│   Stage: Clean                       │
│   (mvn clean)                        │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│   Stage: Build                       │
│   (mvn package -DskipTests)          │
└──────────────┬───────────────────────┘
               │
        ┌──────┴──────┐
        │ FAILURE     │ SUCCESS
        ▼             ▼
    ❌ Email   ┌──────────────────────────────────────┐
    srengty@  │   Stage: Test                        │
    gmail.com │   (mvn test + SQLite DB)             │
              └──────────────┬───────────────────────┘
                             │
                      ┌──────┴──────┐
                      │ FAILURE     │ SUCCESS
                      ▼             ▼
                  ❌ Email   ┌──────────────────────────────────────┐
                  srengty@  │   Stage: Deploy (Ansible)            │
                  gmail.com │   (Run Ansible playbook)             │
                  +         └──────────────┬───────────────────────┘
                  author                   │
                                           ▼
                                   ✅ Email Success
                                   srengty@gmail.com
                                   + all developers
```

---

## Troubleshooting

### Jenkins Won't Start
```cmd
# Check logs
cd "C:\Program Files\Jenkins\logs"
type jenkins.log

# Or run as console for debugging
java -jar jenkins.war --httpPort=8080
```

### Email Not Sending
1. Check Extended E-mail log in build console
2. Verify SMTP credentials
3. Check firewall allows port 587
4. Gmail: Use App Password, not regular password

### Git Connection Failed
1. Verify Git is installed: `git --version`
2. Check GitHub credentials
3. Test manual clone: `git clone <repo-url>`

### Ansible Playbook Not Running
1. Ensure Ansible is in PATH: `ansible --version`
2. Test inventory: `ansible-inventory -i inventory --list`
3. Check playbook syntax: `ansible-playbook --syntax-check ansible-playbook.yml`

---

## Manual Trigger

To manually trigger build without waiting 5 minutes:
1. Open Jenkins at `http://localhost:8080`
2. Click on job name
3. Click **Build Now**

---

## View Build Results

1. Click on build number (e.g., #1)
2. Click **Console Output** to see full logs
3. Click **Email** section to see notification details
4. Check email inbox for notifications

---

## Next Steps

1. ✅ Jenkins is now configured
2. ✅ Git polling is active (every 5 minutes)
3. ✅ Build/Test runs automatically
4. ✅ Emails sent on success/failure
5. ✅ Ansible deployment runs on success

Your CI/CD pipeline is now fully operational!
