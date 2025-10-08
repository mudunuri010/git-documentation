'use client'
import React, { useState, useMemo } from 'react';
import { Search, Menu, X, ChevronRight, Home, Book, GitBranch, GitMerge, GitPullRequest, Shield, AlertCircle, Settings, Code, Terminal, BookOpen, Zap } from 'lucide-react';

const GitScenariosDocumentation = () => {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeSection, setActiveSection] = useState('introduction');

  const documentation = {
    introduction: {
      title: 'Introduction',
      icon: Home,
      content: `# Git Scenarios Documentation

Welcome to the comprehensive Git scenarios guide. This documentation covers real-world Git situations you'll encounter in your development workflow.

## What You'll Learn

- **Common Scenarios** - Daily Git operations and best practices
- **Conflict Resolution** - How to handle and resolve merge conflicts
- **Branch Management** - Strategies for effective branching
- **Emergency Fixes** - Quick solutions when things go wrong
- **Team Collaboration** - Working with multiple developers

## Why This Guide?

Git can be complex, but most developers face similar challenges. This guide provides practical solutions to real scenarios you'll encounter, with step-by-step instructions and explanations.

## Quick Start

Choose a scenario from the sidebar that matches your current situation. Each section includes commands, explanations, and best practices.`
    },
    'basic-setup': {
      title: 'Basic Setup',
      icon: Settings,
      content: `# Git Basic Setup

## Initial Configuration

Configure Git with your identity:

\`\`\`bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
\`\`\`

## Set Default Branch Name

\`\`\`bash
git config --global init.defaultBranch main
\`\`\`

## Configure Line Endings

**For Windows:**
\`\`\`bash
git config --global core.autocrlf true
\`\`\`

**For Mac/Linux:**
\`\`\`bash
git config --global core.autocrlf input
\`\`\`

## Setup Default Editor

\`\`\`bash
git config --global core.editor "code --wait"  # VS Code
git config --global core.editor "vim"          # Vim
\`\`\`

## View Your Configuration

\`\`\`bash
git config --list
git config user.name  # View specific setting
\`\`\`

## SSH Key Setup

**Generate SSH key:**
\`\`\`bash
ssh-keygen -t ed25519 -C "your.email@example.com"
\`\`\`

**Add to SSH agent:**
\`\`\`bash
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519
\`\`\`

**Copy public key:**
\`\`\`bash
cat ~/.ssh/id_ed25519.pub
\`\`\`

Then add this key to GitHub/GitLab/Bitbucket in Settings > SSH Keys.`
    },
    'starting-project': {
      title: 'Starting a Project',
      icon: BookOpen,
      content: `# Starting a New Git Project

## Scenario 1: Initialize New Repository

**When:** Starting a brand new project

\`\`\`bash
mkdir my-project
cd my-project
git init
\`\`\`

Create a README and make first commit:
\`\`\`bash
echo "# My Project" > README.md
git add README.md
git commit -m "Initial commit"
\`\`\`

## Scenario 2: Clone Existing Repository

**When:** Joining an existing project

\`\`\`bash
git clone https://github.com/username/repo.git
cd repo
\`\`\`

**Clone specific branch:**
\`\`\`bash
git clone -b develop https://github.com/username/repo.git
\`\`\`

## Scenario 3: Connect Local Repo to Remote

**When:** You have local code and want to push to GitHub

\`\`\`bash
git remote add origin https://github.com/username/repo.git
git branch -M main
git push -u origin main
\`\`\`

## Scenario 4: Fork and Clone

**When:** Contributing to open source

1. Fork repository on GitHub
2. Clone your fork:
\`\`\`bash
git clone https://github.com/YOUR-USERNAME/repo.git
cd repo
\`\`\`

3. Add upstream remote:
\`\`\`bash
git remote add upstream https://github.com/ORIGINAL-OWNER/repo.git
git fetch upstream
\`\`\`

## .gitignore Setup

Create .gitignore file:
\`\`\`bash
# Node.js
node_modules/
npm-debug.log
.env

# Python
__pycache__/
*.pyc
venv/

# IDEs
.vscode/
.idea/
*.swp

# OS
.DS_Store
Thumbs.db
\`\`\``
    },
    'daily-workflow': {
      title: 'Daily Workflow',
      icon: Zap,
      content: `# Daily Git Workflow Scenarios

## Scenario 1: Starting Your Day

**Pull latest changes before starting work:**

\`\`\`bash
git checkout main
git pull origin main
git checkout -b feature/new-feature
\`\`\`

## Scenario 2: Making Changes

**Working on your feature:**

\`\`\`bash
# Make changes to files
git status                    # See what changed
git diff                      # See exact changes
git add .                     # Stage all changes
git add file.txt              # Stage specific file
git commit -m "Add feature X"
\`\`\`

## Scenario 3: Commit Message Best Practices

**Good commit messages:**
\`\`\`bash
git commit -m "Add user authentication feature"
git commit -m "Fix: Resolve login timeout issue"
git commit -m "Refactor: Improve database query performance"
git commit -m "Docs: Update API documentation"
\`\`\`

**Multi-line commits:**
\`\`\`bash
git commit -m "Add payment processing" -m "- Integrate Stripe API
- Add payment validation
- Create transaction history"
\`\`\`

## Scenario 4: Amending Last Commit

**Forgot to add a file:**
\`\`\`bash
git add forgotten-file.txt
git commit --amend --no-edit
\`\`\`

**Change commit message:**
\`\`\`bash
git commit --amend -m "Better commit message"
\`\`\`

## Scenario 5: Viewing History

\`\`\`bash
git log                       # Full history
git log --oneline            # Compact view
git log --graph --oneline    # Visual branch graph
git log -n 5                 # Last 5 commits
git log --author="John"      # Commits by author
git log --since="2 weeks ago"
\`\`\`

## Scenario 6: Undoing Changes

**Undo changes in working directory:**
\`\`\`bash
git checkout -- file.txt     # Discard changes to file
git restore file.txt         # (Git 2.23+)
\`\`\`

**Unstage files:**
\`\`\`bash
git reset HEAD file.txt      # Unstage file
git restore --staged file.txt # (Git 2.23+)
\`\`\`

**Undo last commit (keep changes):**
\`\`\`bash
git reset --soft HEAD~1
\`\`\`

**Undo last commit (discard changes):**
\`\`\`bash
git reset --hard HEAD~1
\`\`\`

## Scenario 7: Stashing Changes

**Need to switch branches but not ready to commit:**

\`\`\`bash
git stash                    # Save current changes
git stash save "WIP: feature X"  # Save with message
git checkout other-branch    # Switch branches
git checkout feature-branch  # Come back
git stash pop                # Restore changes
\`\`\`

**View stashes:**
\`\`\`bash
git stash list
git stash show               # Show latest stash
git stash apply stash@{0}    # Apply specific stash
git stash drop stash@{0}     # Delete specific stash
git stash clear              # Delete all stashes
\`\`\``
    },
    'branching': {
      title: 'Branching Strategies',
      icon: GitBranch,
      content: `# Git Branching Scenarios

## Scenario 1: Create and Switch Branches

**Create new branch:**
\`\`\`bash
git branch feature/login
git checkout feature/login
\`\`\`

**Create and switch in one command:**
\`\`\`bash
git checkout -b feature/login
git switch -c feature/login    # (Git 2.23+)
\`\`\`

## Scenario 2: Branch from Specific Commit

**When:** Need to branch from older commit

\`\`\`bash
git checkout -b hotfix abc1234
\`\`\`

## Scenario 3: List and Manage Branches

\`\`\`bash
git branch                   # List local branches
git branch -a                # List all branches (including remote)
git branch -r                # List remote branches
git branch -d feature/old    # Delete local branch
git branch -D feature/old    # Force delete
git push origin --delete feature/old  # Delete remote branch
\`\`\`

## Scenario 4: Rename Branch

**Rename current branch:**
\`\`\`bash
git branch -m new-branch-name
\`\`\`

**Rename other branch:**
\`\`\`bash
git branch -m old-name new-name
\`\`\`

**Update remote:**
\`\`\`bash
git push origin :old-name new-name
git push origin -u new-name
\`\`\`

## Scenario 5: Track Remote Branch

**When:** Remote branch exists but not locally

\`\`\`bash
git fetch origin
git checkout -b feature/login origin/feature/login
\`\`\`

**Shorthand:**
\`\`\`bash
git checkout --track origin/feature/login
\`\`\`

## Scenario 6: Common Branching Strategies

**Git Flow:**
- **main** - Production code
- **develop** - Development branch
- **feature/** - New features
- **hotfix/** - Emergency fixes
- **release/** - Release preparation

\`\`\`bash
git checkout develop
git checkout -b feature/user-profile
# Work on feature
git checkout develop
git merge feature/user-profile
\`\`\`

**GitHub Flow (Simpler):**
- **main** - Always deployable
- **feature/** - All features and fixes

\`\`\`bash
git checkout main
git pull origin main
git checkout -b feature/add-search
# Work and push
# Create pull request on GitHub
\`\`\`

## Scenario 7: Compare Branches

\`\`\`bash
git diff main..feature/login           # See differences
git log main..feature/login            # See commits
git log --oneline main..feature/login  # Compact view
\`\`\``
    },
    'merging': {
      title: 'Merging & Conflicts',
      icon: GitMerge,
      content: `# Merging and Conflict Resolution

## Scenario 1: Simple Merge

**When:** Merging feature branch into main

\`\`\`bash
git checkout main
git pull origin main
git merge feature/login
git push origin main
\`\`\`

## Scenario 2: Merge Conflict

**When:** Same lines modified in both branches

**Conflict occurs:**
\`\`\`bash
git merge feature/login
# CONFLICT (content): Merge conflict in file.txt
\`\`\`

**View conflicted files:**
\`\`\`bash
git status
\`\`\`

**Conflict markers in file:**
\`\`\`
<<<<<<< HEAD
Current branch content
=======
Feature branch content
>>>>>>> feature/login
\`\`\`

**Resolve conflict:**
1. Edit file and remove markers
2. Keep desired changes
3. Stage and commit:

\`\`\`bash
git add file.txt
git commit -m "Merge feature/login and resolve conflicts"
\`\`\`

**Abort merge:**
\`\`\`bash
git merge --abort
\`\`\`

## Scenario 3: Use Merge Tool

\`\`\`bash
git mergetool
\`\`\`

**Configure merge tool:**
\`\`\`bash
git config --global merge.tool vscode
git config --global mergetool.vscode.cmd 'code --wait $MERGED'
\`\`\`

## Scenario 4: Three-Way Merge Strategy

**Accept all changes from one side:**

\`\`\`bash
# Accept theirs (incoming changes)
git merge -X theirs feature/login

# Accept ours (current branch)
git merge -X ours feature/login
\`\`\`

## Scenario 5: Squash Merge

**When:** Want to combine all commits into one

\`\`\`bash
git checkout main
git merge --squash feature/login
git commit -m "Add login feature"
\`\`\`

## Scenario 6: No Fast-Forward Merge

**When:** Want explicit merge commit

\`\`\`bash
git merge --no-ff feature/login
\`\`\`

## Scenario 7: Cherry-Pick Specific Commits

**When:** Need specific commits from another branch

\`\`\`bash
git cherry-pick abc1234
git cherry-pick abc1234 def5678  # Multiple commits
\`\`\`

**Resolve cherry-pick conflict:**
\`\`\`bash
# Fix conflicts
git add .
git cherry-pick --continue
# Or abort
git cherry-pick --abort
\`\`\`

## Scenario 8: Rebase Instead of Merge

**When:** Want linear history

\`\`\`bash
git checkout feature/login
git rebase main
\`\`\`

**Resolve rebase conflicts:**
\`\`\`bash
# Fix conflicts
git add .
git rebase --continue
# Or abort
git rebase --abort
\`\`\`

**Interactive rebase:**
\`\`\`bash
git rebase -i HEAD~3  # Last 3 commits
# Edit, squash, reorder commits
\`\`\``
    },
    'remote-operations': {
      title: 'Remote Operations',
      icon: GitPullRequest,
      content: `# Remote Repository Scenarios

## Scenario 1: Pushing Changes

**First push:**
\`\`\`bash
git push -u origin feature/login
\`\`\`

**Regular push:**
\`\`\`bash
git push
git push origin main
\`\`\`

**Force push (dangerous!):**
\`\`\`bash
git push --force
git push --force-with-lease  # Safer option
\`\`\`

## Scenario 2: Pulling Changes

**Pull latest changes:**
\`\`\`bash
git pull origin main
\`\`\`

**Pull equals fetch + merge:**
\`\`\`bash
git fetch origin
git merge origin/main
\`\`\`

**Pull with rebase:**
\`\`\`bash
git pull --rebase origin main
\`\`\`

## Scenario 3: Fetch Without Merging

**When:** Want to see changes before merging

\`\`\`bash
git fetch origin
git log origin/main
git diff main origin/main
git merge origin/main
\`\`\`

## Scenario 4: Multiple Remotes

**Add multiple remotes:**
\`\`\`bash
git remote add origin https://github.com/user/repo.git
git remote add upstream https://github.com/original/repo.git
\`\`\`

**View remotes:**
\`\`\`bash
git remote -v
\`\`\`

**Fetch from upstream:**
\`\`\`bash
git fetch upstream
git merge upstream/main
\`\`\`

**Remove remote:**
\`\`\`bash
git remote remove upstream
\`\`\`

## Scenario 5: Update Forked Repository

**When:** Your fork is behind the original

\`\`\`bash
git checkout main
git fetch upstream
git merge upstream/main
git push origin main
\`\`\`

## Scenario 6: Sync Fork with One Command

\`\`\`bash
git pull upstream main
git push origin main
\`\`\`

## Scenario 7: Change Remote URL

**Switch from HTTPS to SSH:**
\`\`\`bash
git remote set-url origin git@github.com:username/repo.git
\`\`\`

**View remote URL:**
\`\`\`bash
git remote get-url origin
\`\`\`

## Scenario 8: Push All Branches

\`\`\`bash
git push --all origin
\`\`\`

## Scenario 9: Prune Deleted Remote Branches

**When:** Remote branches deleted but still showing locally

\`\`\`bash
git fetch --prune
git remote prune origin
\`\`\``
    },
    'pull-requests': {
      title: 'Pull Requests',
      icon: GitPullRequest,
      content: `# Pull Request Workflow

## Scenario 1: Creating a Pull Request

**Complete workflow:**

\`\`\`bash
# 1. Create feature branch
git checkout main
git pull origin main
git checkout -b feature/new-feature

# 2. Make changes and commit
git add .
git commit -m "Add new feature"

# 3. Push to remote
git push -u origin feature/new-feature

# 4. Create PR on GitHub/GitLab
\`\`\`

## Scenario 2: Updating PR with New Changes

**When:** Reviewer requested changes

\`\`\`bash
# Make changes
git add .
git commit -m "Address review comments"
git push origin feature/new-feature
# PR automatically updates
\`\`\`

## Scenario 3: Update PR with Latest Main

**When:** Main branch has new commits

**Option 1: Merge (creates merge commit):**
\`\`\`bash
git checkout feature/new-feature
git merge main
git push origin feature/new-feature
\`\`\`

**Option 2: Rebase (cleaner history):**
\`\`\`bash
git checkout feature/new-feature
git fetch origin
git rebase origin/main
git push --force-with-lease origin feature/new-feature
\`\`\`

## Scenario 4: Squash Commits Before PR

**When:** Too many small commits

\`\`\`bash
git rebase -i HEAD~5  # Last 5 commits
# In editor, change 'pick' to 'squash' for commits to combine
# Save and edit commit message
git push --force-with-lease origin feature/new-feature
\`\`\`

## Scenario 5: Reviewing Someone's PR

**Checkout PR locally:**
\`\`\`bash
git fetch origin pull/123/head:pr-123
git checkout pr-123
\`\`\`

**Test changes:**
\`\`\`bash
# Run tests, check code
# Switch back
git checkout main
git branch -D pr-123
\`\`\`

## Scenario 6: Collaborate on PR

**When:** Multiple people working on same PR

\`\`\`bash
# Checkout their branch
git fetch origin
git checkout feature/their-feature

# Make changes
git add .
git commit -m "Add improvements"
git push origin feature/their-feature
\`\`\`

## Scenario 7: Fix PR Conflicts

**When:** GitHub shows conflicts

\`\`\`bash
git checkout feature/new-feature
git fetch origin
git merge origin/main
# Resolve conflicts
git add .
git commit -m "Resolve merge conflicts"
git push origin feature/new-feature
\`\`\`

## Scenario 8: Close PR Without Merging

**When:** PR no longer needed

\`\`\`bash
# Close on GitHub, then delete branch
git checkout main
git branch -d feature/old-feature
git push origin --delete feature/old-feature
\`\`\`

## PR Best Practices

**Good PR:**
- Small and focused (one feature/fix)
- Clear title and description
- Links to related issues
- Screenshots for UI changes
- Tests included
- Updated documentation

**PR Template:**
\`\`\`markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
Steps to test changes

## Screenshots
If applicable

## Checklist
- [ ] Tests pass
- [ ] Code follows style guide
- [ ] Documentation updated
\`\`\``
    },
    'emergencies': {
      title: 'Emergency Scenarios',
      icon: AlertCircle,
      content: `# Git Emergency Scenarios

## Scenario 1: Accidentally Committed to Wrong Branch

**Solution:**

\`\`\`bash
# Create correct branch
git branch correct-branch

# Reset current branch
git reset --hard HEAD~1

# Switch to correct branch
git checkout correct-branch
\`\`\`

## Scenario 2: Committed Sensitive Data

**Solution:**

\`\`\`bash
# Remove file from last commit
git rm --cached sensitive-file.txt
git commit --amend --no-edit
git push --force-with-lease

# Remove from history (use with caution)
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch sensitive-file.txt" \
  --prune-empty --tag-name-filter cat -- --all
\`\`\`

**Better solution - use BFG Repo-Cleaner:**
\`\`\`bash
bfg --delete-files sensitive-file.txt
git reflog expire --expire=now --all
git gc --prune=now --aggressive
\`\`\`

## Scenario 3: Pushed to Wrong Remote

**Solution:**

\`\`\`bash
# Delete from wrong remote
git push wrong-remote --delete branch-name

# Push to correct remote
git push correct-remote branch-name
\`\`\`

## Scenario 4: Need to Undo Pushed Commit

**Solution 1 - Revert (safer):**
\`\`\`bash
git revert HEAD
git push origin main
\`\`\`

**Solution 2 - Reset (if no one pulled):**
\`\`\`bash
git reset --hard HEAD~1
git push --force-with-lease origin main
\`\`\`

## Scenario 5: Lost Commits After Reset

**Solution - Use reflog:**

\`\`\`bash
git reflog
# Find your lost commit (e.g., abc1234)
git checkout abc1234
git branch recovered-branch
\`\`\`

## Scenario 6: Merge Conflict Nightmare

**Solution - Start over:**

\`\`\`bash
git merge --abort
git fetch origin
git reset --hard origin/main
\`\`\`

## Scenario 7: Detached HEAD State

**When:** Checked out a commit directly

\`\`\`bash
# Create branch to save work
git branch temp-branch
git checkout temp-branch

# Or discard and return
git checkout main
\`\`\`

## Scenario 8: Corrupted Repository

**Solution:**

\`\`\`bash
# Verify repository
git fsck --full

# Clone again if needed
cd ..
git clone https://github.com/user/repo.git repo-backup
\`\`\`

## Scenario 9: Accidentally Deleted Branch

**Solution:**

\`\`\`bash
# Find branch in reflog
git reflog
# Recreate branch
git checkout -b recovered-branch abc1234
\`\`\`

## Scenario 10: Large File Committed

**Solution - Remove from history:**

\`\`\`bash
# Using BFG
bfg --strip-blobs-bigger-than 50M

# Or git filter-branch
git filter-branch --tree-filter 'rm -f large-file.zip' HEAD
\`\`\`

## Scenario 11: Wrong Commit Message Pushed

**Solution:**

\`\`\`bash
git commit --amend -m "Correct message"
git push --force-with-lease origin branch-name
\`\`\`

## Prevention Tips

**Always:**
- Work on feature branches
- Pull before push
- Use .gitignore
- Review changes before committing
- Use --force-with-lease instead of --force
- Test before pushing
- Create backups of important work

**Never:**
- Force push to main/master
- Commit sensitive data
- Rewrite public history
- Delete branches without backup`
    },
    'advanced': {
      title: 'Advanced Scenarios',
      icon: Code,
      content: `# Advanced Git Scenarios

## Scenario 1: Bisect to Find Bug

**When:** Need to find which commit introduced bug

\`\`\`bash
git bisect start
git bisect bad                 # Current commit is bad
git bisect good abc1234        # Known good commit

# Git checks out middle commit
# Test and mark as good or bad
git bisect good
# or
git bisect bad

# Repeat until found
git bisect reset              # Return to original state
\`\`\`

## Scenario 2: Git Hooks

**When:** Automate tasks on Git events

**Pre-commit hook (.git/hooks/pre-commit):**
\`\`\`bash
#!/bin/sh
npm run lint
npm test
\`\`\`

**Make executable:**
\`\`\`bash
chmod +x .git/hooks/pre-commit
\`\`\`

## Scenario 3: Submodules

**Add submodule:**
\`\`\`bash
git submodule add https://github.com/user/lib.git libs/mylib
git commit -m "Add submodule"
\`\`\`

**Clone with submodules:**
\`\`\`bash
git clone --recursive https://github.com/user/repo.git
\`\`\`

**Update submodules:**
\`\`\`bash
git submodule update --init --recursive
git submodule update --remote
\`\`\`

## Scenario 4: Git Worktrees

**When:** Need multiple branches simultaneously

\`\`\`bash
# Create worktree
git worktree add ../hotfix-branch hotfix/urgent

# List worktrees
git worktree list

# Remove worktree
git worktree remove ../hotfix-branch
\`\`\`

## Scenario 5: Partial Clone

**When:** Repository is huge

\`\`\`bash
# Clone without history
git clone --depth 1 https://github.com/user/repo.git

# Fetch more history later
git fetch --unshallow
\`\`\`

## Scenario 6: Sparse Checkout

**When:** Only need specific files

\`\`\`bash
git clone --filter=blob:none --sparse https://github.com/user/repo.git
cd repo
git sparse-checkout init --cone
git sparse-checkout set docs src/components
\`\`\`

## Scenario 7: Git Attributes

**When:** Need special handling for files

**Create .gitattributes:**
\`\`\`
*.jpg binary
*.png binary
*.pdf binary
*.md text
*.js text eol=lf
*.sql linguist-detectable=true
\`\`\`

## Scenario 8: Custom Aliases

**Time-saving aliases:**

\`\`\`bash
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.st status
git config --global alias.unstage 'reset HEAD --'
git config --global alias.last 'log -1 HEAD'
git config --global alias.visual 'log --graph --oneline --all'
git config --global alias.undo 'reset --soft HEAD~1'
\`\`\`

## Scenario 9: Patch Files

**Create patch:**
\`\`\`bash
git diff > changes.patch
git format-patch -1 HEAD      # Last commit as patch
\`\`\`

**Apply patch:**
\`\`\`bash
git apply changes.patch
git am < changes.patch        # Apply and commit
\`\`\`

## Scenario 10: Blame and Annotate

**When:** Need to find who changed what

\`\`\`bash
git blame file.txt
git blame -L 10,20 file.txt   # Specific lines
git blame -C file.txt         # Detect moved code
\`\`\`

## Scenario 11: Archive Repository

**When:** Need to export code

\`\`\`bash
git archive --format=zip HEAD > project.zip
git archive --format=tar.gz HEAD > project.tar.gz
\`\`\`

## Scenario 12: Clean Untracked Files

\`\`\`bash
git clean -n                  # Dry run
git clean -f                  # Remove files
git clean -fd                 # Remove files and directories
git clean -fX                 # Remove ignored files
git clean -fx                 # Remove ignored and untracked
\`\`\``
    },
    'team-workflows': {
      title: 'Team Workflows',
      icon: Shield,
      content: `# Team Collaboration Scenarios

## Scenario 1: Code Review Workflow

**Reviewer workflow:**
\`\`\`bash
# Fetch PR
git fetch origin pull/123/head:review-pr-123
git checkout review-pr-123

# Review code
git diff main...review-pr-123

# Test locally
npm install
npm test

# Add review comments on GitHub
\`\`\`

## Scenario 2: Protected Branches

**When:** Enforcing code review

**Setup branch protection (GitHub):**
- Require pull request reviews
- Require status checks
- Require branches up to date
- Restrict who can push

## Scenario 3: Release Management

**Git Flow release:**
\`\`\`bash
# Create release branch
git checkout develop
git checkout -b release/1.0.0

# Version bump, final fixes
git commit -m "Bump version to 1.0.0"

# Merge to main
git checkout main
git merge --no-ff release/1.0.0
git tag -a v1.0.0 -m "Release 1.0.0"

# Merge back to develop
git checkout develop
git merge --no-ff release/1.0.0

# Delete release branch
git branch -d release/1.0.0
\`\`\`

## Scenario 4: Hotfix Workflow

**When:** Emergency production fix

\`\`\`bash
# Create hotfix from main
git checkout main
git checkout -b hotfix/critical-bug

# Fix and test
git commit -m "Fix critical bug"

# Merge to main
git checkout main
git merge --no-ff hotfix/critical-bug
git tag -a v1.0.1 -m "Hotfix 1.0.1"

# Merge to develop
git checkout develop
git merge --no-ff hotfix/critical-bug

# Delete hotfix branch
git branch -d hotfix/critical-bug

# Deploy
git push origin main --tags
\`\`\`

## Scenario 5: Pair Programming

**When:** Two developers on one feature

**Driver's setup:**
\`\`\`bash
git config user.name "Dev1 and Dev2"
git commit -m "Feature X

Co-authored-by: Dev2 <dev2@email.com>"
\`\`\`

## Scenario 6: Handling Diverged Branches

**When:** Your branch and remote have diverged

\`\`\`bash
# Check status
git status
# "Your branch and 'origin/feature' have diverged"

# Option 1: Rebase your changes
git pull --rebase origin feature

# Option 2: Merge remote changes
git pull origin feature

# Option 3: Force push (if you're sure)
git push --force-with-lease origin feature
\`\`\`

## Scenario 7: Multiple Developers on Same Branch

**Best practices:**

\`\`\`bash
# Always pull before starting work
git pull origin feature/shared

# Make small, frequent commits
git add .
git commit -m "Small change"
git pull --rebase origin feature/shared
git push origin feature/shared
\`\`\`

## Scenario 8: Feature Flags for Collaboration

**When:** Incomplete feature but need to merge

\`\`\`javascript
// Use feature flags
if (featureFlags.newFeature) {
  // New code
} else {
  // Old code
}
\`\`\`

\`\`\`bash
git commit -m "Add new feature behind feature flag"
git push origin main  # Safe to merge
\`\`\`

## Scenario 9: Conventional Commits

**Team commit convention:**

\`\`\`bash
# Format: <type>(<scope>): <subject>

git commit -m "feat(auth): add OAuth2 login"
git commit -m "fix(api): resolve timeout issue"
git commit -m "docs(readme): update installation steps"
git commit -m "style(button): improve hover effect"
git commit -m "refactor(database): optimize queries"
git commit -m "test(login): add integration tests"
git commit -m "chore(deps): update dependencies"
git commit -m "perf(images): implement lazy loading"
\`\`\`

## Scenario 10: Daily Standup Workflow

**Show your work:**

\`\`\`bash
# What you did yesterday
git log --oneline --author="Your Name" --since="yesterday"

# What you're working on
git branch --show-current

# Current status
git status --short
\`\`\``
    },
    'troubleshooting': {
      title: 'Troubleshooting',
      icon: Terminal,
      content: `# Git Troubleshooting Guide

## Problem 1: "Permission Denied (publickey)"

**Symptoms:** Can't push/pull from remote

**Solutions:**

\`\`\`bash
# Check SSH connection
ssh -T git@github.com

# Generate new SSH key
ssh-keygen -t ed25519 -C "your_email@example.com"

# Start SSH agent
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519

# Add key to GitHub/GitLab
cat ~/.ssh/id_ed25519.pub
\`\`\`

## Problem 2: "Fatal: Not a Git Repository"

**Symptoms:** Git commands don't work

**Solutions:**

\`\`\`bash
# Initialize repository
git init

# Or check if you're in correct directory
pwd
cd /path/to/project

# Or clone repository
git clone https://github.com/user/repo.git
\`\`\`

## Problem 3: "Your Branch is Behind"

**Symptoms:** Can't push, need to pull first

**Solutions:**

\`\`\`bash
# Pull and merge
git pull origin main

# Or pull with rebase
git pull --rebase origin main

# Force push (if you're certain)
git push --force-with-lease origin main
\`\`\`

## Problem 4: "Merge Conflict"

**Symptoms:** Conflicts after merge/pull

**Solutions:**

\`\`\`bash
# View conflicted files
git status

# Open and edit files, remove markers:
# <<<<<<< HEAD
# =======
# >>>>>>>

# After fixing
git add .
git commit

# Or abort merge
git merge --abort
\`\`\`

## Problem 5: "Detached HEAD"

**Symptoms:** Not on any branch

**Solutions:**

\`\`\`bash
# Create branch from current state
git checkout -b new-branch-name

# Or return to main
git checkout main
\`\`\`

## Problem 6: Large File Error

**Symptoms:** "File exceeds GitHub's 100MB limit"

**Solutions:**

\`\`\`bash
# Use Git LFS
git lfs install
git lfs track "*.zip"
git add .gitattributes
git commit -m "Configure Git LFS"

# Or remove file from history
git filter-branch --tree-filter 'rm -f large-file.zip' HEAD
\`\`\`

## Problem 7: "Updates Were Rejected"

**Symptoms:** Push rejected, non-fast-forward

**Solutions:**

\`\`\`bash
# Pull first
git pull origin main

# Resolve any conflicts, then push
git push origin main

# Force with lease (safer)
git push --force-with-lease origin main
\`\`\`

## Problem 8: Uncommitted Changes When Switching

**Symptoms:** Can't checkout, have uncommitted changes

**Solutions:**

\`\`\`bash
# Stash changes
git stash
git checkout other-branch

# Return and restore
git checkout original-branch
git stash pop

# Or commit changes
git add .
git commit -m "WIP"
\`\`\`

## Problem 9: Wrong Author on Commits

**Symptoms:** Commits show wrong name/email

**Solutions:**

\`\`\`bash
# Fix for next commits
git config user.name "Correct Name"
git config user.email "correct@email.com"

# Amend last commit
git commit --amend --author="Name <email@example.com>" --no-edit

# Fix multiple commits
git rebase -i HEAD~3
# Mark commits as 'edit'
git commit --amend --author="Name <email@example.com>" --no-edit
git rebase --continue
\`\`\`

## Problem 10: Slow Git Operations

**Symptoms:** Git commands are slow

**Solutions:**

\`\`\`bash
# Optimize repository
git gc --aggressive
git prune

# Check repository size
du -sh .git

# Shallow clone for large repos
git clone --depth 1 url
\`\`\`

## Problem 11: ".gitignore Not Working"

**Symptoms:** Ignored files still tracked

**Solutions:**

\`\`\`bash
# Remove from Git cache
git rm -r --cached .
git add .
git commit -m "Fix .gitignore"

# For specific file
git rm --cached file.txt
git commit -m "Stop tracking file.txt"
\`\`\`

## Problem 12: Lost Commits

**Symptoms:** Commits disappeared after reset

**Solutions:**

\`\`\`bash
# Find lost commits
git reflog

# Recover commit
git checkout abc1234
git branch recovered-branch

# Or cherry-pick
git cherry-pick abc1234
\`\`\`

## Problem 13: Line Ending Issues

**Symptoms:** Files show as modified but no changes

**Solutions:**

\`\`\`bash
# Configure line endings
# Windows
git config --global core.autocrlf true

# Mac/Linux
git config --global core.autocrlf input

# Normalize repository
git add --renormalize .
git commit -m "Normalize line endings"
\`\`\`

## Problem 14: Can't Delete Branch

**Symptoms:** "Branch not fully merged"

**Solutions:**

\`\`\`bash
# Force delete local branch
git branch -D branch-name

# Delete remote branch
git push origin --delete branch-name

# Verify deletion
git branch -a
\`\`\`

## Problem 15: Authentication Keeps Failing

**Symptoms:** Always prompted for credentials

**Solutions:**

\`\`\`bash
# Use SSH instead of HTTPS
git remote set-url origin git@github.com:user/repo.git

# Or use credential helper
git config --global credential.helper cache

# For Windows
git config --global credential.helper wincred

# Store credentials (insecure)
git config --global credential.helper store
\`\`\``
    },
    'best-practices': {
      title: 'Best Practices',
      icon: Book,
      content: `# Git Best Practices

## Commit Best Practices

**Good commits:**
- Small and focused
- Working code only
- Clear commit messages
- Logical grouping of changes

\`\`\`bash
# Good
git commit -m "Add user login validation"

# Bad
git commit -m "fixes"
git commit -m "updated stuff"
\`\`\`

## Branch Naming Conventions

**Consistent naming:**
\`\`\`
feature/user-authentication
feature/add-payment-gateway
bugfix/fix-login-timeout
bugfix/resolve-memory-leak
hotfix/critical-security-patch
release/v1.2.0
docs/update-api-documentation
refactor/improve-database-queries
test/add-integration-tests
\`\`\`

## .gitignore Best Practices

**Comprehensive .gitignore:**
\`\`\`
# Dependencies
node_modules/
vendor/
*.lock

# Environment
.env
.env.local
.env.*.local

# Build outputs
dist/
build/
*.min.js
*.min.css

# IDE
.vscode/
.idea/
*.swp
*.swo

# OS
.DS_Store
Thumbs.db
desktop.ini

# Logs
*.log
npm-debug.log*
logs/

# Database
*.sqlite
*.db

# Temporary
tmp/
temp/
*.tmp
\`\`\`

## Pull Request Guidelines

**Quality PRs:**
- Single responsibility
- Descriptive title
- Detailed description
- Link to issue
- Screenshots for UI
- Tests included
- Documentation updated

**PR Description Template:**
\`\`\`markdown
## What
Brief description of changes

## Why
Reason for the change

## How
Technical approach

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

## Screenshots
[If applicable]

## Related Issues
Closes #123
\`\`\`

## Commit Message Format

**Convention:**
\`\`\`
<type>(<scope>): <subject>

<body>

<footer>
\`\`\`

**Example:**
\`\`\`
feat(auth): implement JWT authentication

- Add JWT token generation
- Create middleware for verification
- Update user model with tokens

Closes #42
\`\`\`

**Types:**
- **feat** - New feature
- **fix** - Bug fix
- **docs** - Documentation
- **style** - Formatting
- **refactor** - Code restructuring
- **test** - Adding tests
- **chore** - Maintenance

## Repository Structure

**Organized repository:**
\`\`\`
project/
├── .github/
│   ├── workflows/      # CI/CD
│   ├── ISSUE_TEMPLATE/
│   └── pull_request_template.md
├── docs/              # Documentation
├── src/               # Source code
├── tests/             # Test files
├── .gitignore
├── README.md
├── CONTRIBUTING.md
└── LICENSE
\`\`\`

## Security Best Practices

**Never commit:**
- API keys
- Passwords
- Private keys
- Database credentials
- Access tokens
- Environment variables

**Use:**
- .env files (and .gitignore them)
- Secret management tools
- Environment variable injection

## Code Review Checklist

**Before requesting review:**
- [ ] Code compiles/runs
- [ ] Tests pass
- [ ] No console.log/debug code
- [ ] Comments added for complex logic
- [ ] Documentation updated
- [ ] No sensitive data
- [ ] Branch up to date with main

**During review:**
- Be respectful
- Ask questions
- Suggest, don't demand
- Approve or request changes
- Test the code

## Git Workflow Rules

**Daily workflow:**
1. Pull before starting work
2. Create feature branch
3. Make small commits
4. Push regularly
5. Create PR when ready
6. Address review comments
7. Merge and delete branch

**Team rules:**
- Never force push to main
- Always use pull requests
- Require code review
- Keep branches short-lived
- Delete merged branches
- Communicate with team

## Documentation Standards

**README.md should include:**
- Project description
- Installation instructions
- Usage examples
- Configuration options
- Contributing guidelines
- License information

**CONTRIBUTING.md should include:**
- How to set up development environment
- Coding standards
- Commit message format
- PR process
- Issue reporting

## Performance Tips

**Keep repository clean:**
\`\`\`bash
# Regular maintenance
git gc --auto
git prune

# Check size
git count-objects -vH

# Remove old branches
git remote prune origin
\`\`\`

**Optimize for large repositories:**
- Use shallow clones
- Use sparse checkout
- Enable Git LFS for large files
- Archive old branches

## Backup Strategies

**Regular backups:**
\`\`\`bash
# Clone with all branches
git clone --mirror url backup.git

# Backup to different remote
git remote add backup backup-url
git push backup --all
git push backup --tags
\`\`\`

## Common Mistakes to Avoid

**Don't:**
- Commit directly to main
- Use generic commit messages
- Commit broken code
- Ignore merge conflicts
- Delete branches before merging
- Force push without --force-with-lease
- Commit large binary files
- Rewrite public history
- Skip code review
- Leave branches open indefinitely`
    }
  };

  const filteredDocs = useMemo(() => {
    if (!searchQuery) return documentation;
    
    const filtered = {};
    Object.entries(documentation).forEach(([key, value]) => {
      if (
        value.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        value.content.toLowerCase().includes(searchQuery.toLowerCase())
      ) {
        filtered[key] = value;
      }
    });
    return filtered;
  }, [searchQuery]);

  const renderMarkdown = (content) => {
    const lines = content.split('\n');
    let inCodeBlock = false;
    let codeBlockContent = [];
    let codeLanguage = '';

    return lines.map((line, i) => {
      // Code blocks
      if (line.startsWith('```')) {
        if (!inCodeBlock) {
          inCodeBlock = true;
          codeLanguage = line.slice(3);
          codeBlockContent = [];
          return null;
        } else {
          inCodeBlock = false;
          const code = codeBlockContent.join('\n');
          codeBlockContent = [];
          return (
            <pre key={i} className="bg-gray-900 text-gray-100 p-4 rounded-lg overflow-x-auto my-4">
              <code className="text-sm font-mono">{code}</code>
            </pre>
          );
        }
      }

      if (inCodeBlock) {
        codeBlockContent.push(line);
        return null;
      }

      // Headers
      if (line.startsWith('# ')) {
        return <h1 key={i} className="text-3xl font-bold mb-4 mt-8 text-gray-900">{line.slice(2)}</h1>;
      }
      if (line.startsWith('## ')) {
        return <h2 key={i} className="text-2xl font-semibold mb-3 mt-6 text-gray-900">{line.slice(3)}</h2>;
      }
      if (line.startsWith('### ')) {
        return <h3 key={i} className="text-xl font-semibold mb-2 mt-5 text-gray-800">{line.slice(4)}</h3>;
      }
      
      // Bold text with inline code
      if (line.includes('**') || line.includes('`')) {
        const processedLine = [];
        let remainingLine = line;
        let key = 0;

        while (remainingLine.length > 0) {
          const boldIndex = remainingLine.indexOf('**');
          const codeIndex = remainingLine.indexOf('`');

          if (boldIndex === -1 && codeIndex === -1) {
            processedLine.push(remainingLine);
            break;
          }

          if (boldIndex !== -1 && (codeIndex === -1 || boldIndex < codeIndex)) {
            if (boldIndex > 0) {
              processedLine.push(remainingLine.slice(0, boldIndex));
            }
            const endBold = remainingLine.indexOf('**', boldIndex + 2);
            if (endBold !== -1) {
              processedLine.push(<strong key={key++}>{remainingLine.slice(boldIndex + 2, endBold)}</strong>);
              remainingLine = remainingLine.slice(endBold + 2);
            } else {
              remainingLine = remainingLine.slice(boldIndex + 2);
            }
          } else {
            if (codeIndex > 0) {
              processedLine.push(remainingLine.slice(0, codeIndex));
            }
            const endCode = remainingLine.indexOf('`', codeIndex + 1);
            if (endCode !== -1) {
              processedLine.push(
                <code key={key++} className="bg-gray-100 px-2 py-0.5 rounded text-sm font-mono text-purple-600">
                  {remainingLine.slice(codeIndex + 1, endCode)}
                </code>
              );
              remainingLine = remainingLine.slice(endCode + 1);
            } else {
              remainingLine = remainingLine.slice(codeIndex + 1);
            }
          }
        }

        return <p key={i} className="mb-2 text-gray-700 leading-relaxed">{processedLine}</p>;
      }
      
      // List items
      if (line.trim().startsWith('- ') || line.trim().startsWith('* ')) {
        return <li key={i} className="ml-6 mb-1 text-gray-700 list-disc">{line.trim().slice(2)}</li>;
      }
      if (/^\d+\./.test(line.trim())) {
        return <li key={i} className="ml-6 mb-1 text-gray-700 list-decimal">{line.trim().replace(/^\d+\.\s*/, '')}</li>;
      }
      
      // Regular paragraph
      if (line.trim()) {
        return <p key={i} className="mb-2 text-gray-700 leading-relaxed">{line}</p>;
      }
      
      return <br key={i} />;
    });
  };

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <aside className={`${sidebarOpen ? 'w-72' : 'w-0'} bg-white border-r border-gray-200 transition-all duration-300 overflow-hidden flex flex-col`}>
        <div className="p-6 border-b border-gray-200">
          <h2 className="text-2xl font-bold text-gray-900">Git Scenarios</h2>
          <p className="text-sm text-gray-500 mt-1">Complete Reference Guide</p>
        </div>
        
        <nav className="p-4 flex-1 overflow-y-auto">
          {Object.entries(filteredDocs).map(([key, section]) => {
            const Icon = section.icon;
            return (
              <button
                key={key}
                onClick={() => setActiveSection(key)}
                className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg mb-2 transition-colors ${
                  activeSection === key
                    ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-700'
                    : 'text-gray-700 hover:bg-gray-50'
                }`}
              >
                <Icon size={20} />
                <span className="text-sm font-medium text-left">{section.title}</span>
                {activeSection === key && <ChevronRight size={16} className="ml-auto" />}
              </button>
            );
          })}
        </nav>

        <div className="p-4 border-t border-gray-200 text-xs text-gray-500">
          <p>💡 Tip: Use Ctrl+F to search</p>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col overflow-hidden">
        {/* Header */}
        <header className="bg-white border-b border-gray-200 px-6 py-4 shadow-sm">
          <div className="flex items-center gap-4">
            <button
              onClick={() => setSidebarOpen(!sidebarOpen)}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
              aria-label="Toggle sidebar"
            >
              {sidebarOpen ? <X size={22} /> : <Menu size={22} />}
            </button>
            
            <div className="flex-1 max-w-2xl relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
              <input
                type="text"
                placeholder="Search Git scenarios, commands, or solutions..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-11 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div className="hidden md:flex items-center gap-2 text-sm text-gray-600">
              <GitBranch size={16} />
              <span>Git Documentation</span>
            </div>
          </div>
        </header>

        {/* Content Area */}
        <div className="flex-1 overflow-auto bg-gray-50">
          <div className="max-w-5xl mx-auto px-8 py-10">
            {documentation[activeSection] && (
              <article className="bg-white rounded-lg shadow-sm p-8">
                {renderMarkdown(documentation[activeSection].content)}
              </article>
            )}
            
            {Object.keys(filteredDocs).length === 0 && (
              <div className="bg-white rounded-lg shadow-sm p-12 text-center">
                <Search size={56} className="mx-auto text-gray-300 mb-4" />
                <h3 className="text-2xl font-semibold text-gray-700 mb-2">No results found</h3>
                <p className="text-gray-500">Try searching for different keywords like "merge", "branch", "conflict"</p>
              </div>
            )}
          </div>
        </div>

        {/* Footer */}
        <footer className="bg-white border-t border-gray-200 px-6 py-4">
          <div className="max-w-5xl mx-auto flex flex-col md:flex-row justify-between items-center gap-4 text-sm text-gray-600">
            <p>© 2024 Git Scenarios Documentation. All scenarios tested and verified.</p>
            <div className="flex gap-6">
              <a href="https://git-scm.com/doc" target="_blank" rel="noopener noreferrer" className="hover:text-blue-600 transition-colors">Official Git Docs</a>
              <a href="https://github.com" target="_blank" rel="noopener noreferrer" className="hover:text-blue-600 transition-colors">GitHub</a>
              <a href="#" className="hover:text-blue-600 transition-colors">Report Issue</a>
            </div>
          </div>
        </footer>
      </main>
    </div>
  );
};

export default GitScenariosDocumentation;