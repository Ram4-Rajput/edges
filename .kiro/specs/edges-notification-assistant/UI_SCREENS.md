# EDGES App - UI/UX Screen Designs

## Screen Flow

```
Splash Screen → Permission Setup → Home Screen ⟷ Meetings Screen
```

---

## 1. Splash/Welcome Screen

**Purpose**: First screen users see when opening the app

**Layout**:
```
┌─────────────────────────────┐
│                             │
│                             │
│         [APP LOGO]          │
│           EDGES             │
│                             │
│   AI-Powered Meeting        │
│      Assistant              │
│                             │
│                             │
│    [Get Started Button]     │
│                             │
│                             │
└─────────────────────────────┘
```

**Elements**:
- App logo (centered, large)
- App name "EDGES" (bold, purple #6c5cff)
- Tagline "AI-Powered Meeting Assistant" (gray text)
- "Get Started" button (purple, rounded, full-width)

**Behavior**:
- Shows for 1-2 seconds on first launch
- Button navigates to Permission Setup screen
- On subsequent launches, goes directly to Home if permissions granted

---

## 2. Permission Setup Screen

**Purpose**: Guide users through granting required permissions

**Layout**:
```
┌─────────────────────────────┐
│  Setup Permissions (1/3)    │
│                             │
│  ┌───────────────────────┐  │
│  │  [Icon] Notifications │  │
│  │  Allow EDGES to read  │  │
│  │  your notifications   │  │
│  │                       │  │
│  │  [Grant Permission]   │  │
│  │  ✓ Granted / ⚠ Needed │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │  [Icon] Calendar      │  │
│  │  Allow EDGES to add   │  │
│  │  events to calendar   │  │
│  │                       │  │
│  │  [Grant Permission]   │  │
│  │  ✓ Granted / ⚠ Needed │  │
│  └───────────────────────┘  │
│                             │
│       [Continue Button]     │
└─────────────────────────────┘
```

**Permission Cards** (3 total):

1. **Notification Access** (Required)
   - Icon: Bell icon
   - Title: "Notification Access"
   - Description: "Allow EDGES to read your notifications to detect meetings"
   - Button: "Grant Permission" → Opens system settings
   - Status: Green checkmark if granted, orange warning if not

2. **Calendar Access** (Required)
   - Icon: Calendar icon
   - Title: "Calendar Access"
   - Description: "Allow EDGES to add events to your calendar"
   - Button: "Grant Permission" → Requests permission
   - Status: Green checkmark if granted, orange warning if not

3. **Post Notifications** (Required for Android 13+)
   - Icon: Notification icon
   - Title: "Show Notifications"
   - Description: "Allow EDGES to notify you about scheduled meetings"
   - Button: "Grant Permission" → Requests permission
   - Status: Green checkmark if granted, orange warning if not

**Behavior**:
- "Continue" button is disabled until required permissions granted
- Each "Grant Permission" button opens appropriate system dialog
- Progress indicator shows "1/3", "2/3", "3/3" as permissions are granted
- After all granted, "Continue" navigates to Home screen

---

## 3. Home Screen

**Purpose**: Main dashboard showing stats and meeting suggestions

**Layout**:
```
┌─────────────────────────────┐
│  EDGES          [Settings]  │
│                             │
│  ┌─────┐ ┌─────┐ ┌─────┐   │
│  │ 24  │ │  5  │ │  ✓  │   │
│  │Notif│ │Meet │ │Activ│   │
│  └─────┘ └─────┘ └─────┘   │
│                             │
│  Meeting Suggestions        │
│                             │
│  ┌───────────────────────┐  │
│  │ Team Standup          │  │
│  │ Mar 15, 2PM • 30 min  │  │
│  │                       │  │
│  │ AI Confidence: 85% 🟢 │  │
│  │ Why:                  │  │
│  │ • Clear date & time   │  │
│  │ • Known attendees     │  │
│  │ • Regular meeting     │  │
│  │                       │  │
│  │ [Schedule] [Dismiss]  │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │ Project Review        │  │
│  │ Tomorrow, 3PM • 1 hr  │  │
│  │                       │  │
│  │ AI Confidence: 65% 🟡 │  │
│  │ Why:                  │  │
│  │ • Unclear location    │  │
│  │ • Needs confirmation  │  │
│  │                       │  │
│  │ [Schedule] [Dismiss]  │  │
│  └───────────────────────┘  │
│                             │
│ [Home] [Meetings]           │
└─────────────────────────────┘
```

**Top Bar**:
- App name "EDGES" (left)
- Settings icon (right) - opens settings dialog

**Stats Cards** (3 cards in a row):
1. **Total Notifications**: Shows count of notifications captured today
2. **Meetings Detected**: Shows count of meetings detected
3. **Service Status**: Shows green checkmark if service is active

**Meeting Suggestion Cards**:
Each card shows:
- **Meeting Title** (bold, large text)
- **Date/Time & Duration** (gray text, smaller)
- **AI Confidence Badge**:
  - 🟢 Green (80-100%): "AI Confidence: 85%"
  - 🟡 Yellow (50-79%): "AI Confidence: 65%"
  - 🔴 Red (<50%): Not shown (auto-dismissed)
- **AI Reasoning** (bullet points):
  - "Why:" label
  - 2-3 bullet points explaining the decision
  - Examples: "Clear date & time", "Known attendees", "Regular meeting pattern"
- **Action Buttons**:
  - "Schedule" button (purple, left)
  - "Dismiss" button (gray outline, right)

**Bottom Navigation**:
- "Home" tab (active, purple)
- "Meetings" tab (inactive, gray)

**Behavior**:
- Pull to refresh updates the list
- Tapping "Schedule" creates calendar event and shows success message
- Tapping "Dismiss" removes card with animation
- Empty state shows "No meeting suggestions" with icon

---

## 4. Meetings Screen

**Purpose**: View all meetings (pending, scheduled, dismissed)

**Layout**:
```
┌─────────────────────────────┐
│  All Meetings               │
│                             │
│  [Pending] [Scheduled] [All]│
│                             │
│  ┌───────────────────────┐  │
│  │ ✓ Team Standup        │  │
│  │ Mar 15, 2PM           │  │
│  │ Scheduled             │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │ ⏰ Project Review      │  │
│  │ Mar 16, 3PM           │  │
│  │ Pending               │  │
│  │ [Schedule] [Dismiss]  │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │ ✕ Weekly Sync         │  │
│  │ Mar 14, 10AM          │  │
│  │ Dismissed             │  │
│  └───────────────────────┘  │
│                             │
│ [Home] [Meetings]           │
└─────────────────────────────┘
```

**Filter Tabs**:
- "Pending" - Shows meetings awaiting user action
- "Scheduled" - Shows meetings added to calendar
- "All" - Shows all meetings

**Meeting List Items**:
Each item shows:
- **Status Icon**:
  - ✓ Green checkmark for scheduled
  - ⏰ Clock for pending
  - ✕ Gray X for dismissed
- **Meeting Title** (bold)
- **Date/Time** (gray text)
- **Status Label** (colored text)
- **Action Buttons** (only for pending):
  - "Schedule" button
  - "Dismiss" button

**Bottom Navigation**:
- "Home" tab (inactive, gray)
- "Meetings" tab (active, purple)

**Behavior**:
- Tapping a meeting shows details dialog
- Filter tabs update the list
- Empty state shows "No meetings" with icon

---

## 5. Meeting Details Dialog

**Purpose**: Show full meeting details when user taps a card

**Layout**:
```
┌─────────────────────────────┐
│  Meeting Details      [X]   │
│                             │
│  Team Standup               │
│                             │
│  📅 March 15, 2024          │
│  🕐 2:00 PM - 2:30 PM       │
│  📍 Conference Room A       │
│  👥 john@company.com        │
│      sarah@company.com      │
│                             │
│  AI Analysis:               │
│  Confidence: 85% 🟢         │
│                             │
│  Reasoning:                 │
│  • Clear date and time      │
│  • Known attendees          │
│  • Regular meeting pattern  │
│  • Location specified       │
│                             │
│  From: WhatsApp             │
│  "Team standup tomorrow at  │
│   2 PM in Conference Room A"│
│                             │
│  [Schedule Meeting]         │
│  [Dismiss]                  │
└─────────────────────────────┘
```

**Elements**:
- Close button (X) in top right
- Meeting title (large, bold)
- Date icon + formatted date
- Time icon + time range
- Location icon + location (if available)
- Attendees icon + email list (if available)
- AI Analysis section:
  - Confidence percentage with colored badge
  - Reasoning bullet points
- Original notification:
  - Source app name
  - Original notification text (quoted)
- Action buttons at bottom

**Behavior**:
- Tapping "Schedule Meeting" creates calendar event
- Tapping "Dismiss" marks as dismissed
- Tapping [X] or outside dialog closes it
- Shows success/error message after action

---

## 6. Success/Error Messages

**Success Message** (Toast/Snackbar):
```
┌─────────────────────────────┐
│ ✓ Meeting scheduled!        │
│   Added to your calendar    │
└─────────────────────────────┘
```

**Error Message** (Toast/Snackbar):
```
┌─────────────────────────────┐
│ ⚠ Failed to schedule        │
│   Tap to retry              │
└─────────────────────────────┘
```

---

## Design System

**Colors**:
- Primary: #6c5cff (Purple) - buttons, active states
- Success: #10b981 (Green) - high confidence, success messages
- Warning: #f59e0b (Yellow/Orange) - medium confidence
- Error: #ef4444 (Red) - errors, low confidence
- Background: #f6f5f8 (Light gray)
- Surface: #ffffff (White) - cards
- Text Primary: #1e293b (Dark gray)
- Text Secondary: #64748b (Medium gray)

**Typography**:
- Title: 24sp, Bold
- Heading: 20sp, SemiBold
- Body: 16sp, Regular
- Caption: 14sp, Regular
- Small: 12sp, Regular

**Spacing**:
- Small: 8dp
- Medium: 16dp
- Large: 24dp

**Components**:
- Cards: White background, 16dp corner radius, subtle shadow
- Buttons: 12dp corner radius, 48dp height
- Icons: 24dp size
- Badges: Rounded pill shape with colored background

---

## User Interactions

**Meeting Card Actions**:
1. User sees meeting suggestion card
2. Reads AI confidence and reasoning
3. Decides to schedule or dismiss
4. Taps button
5. Sees confirmation message
6. Card updates or disappears

**Permission Flow**:
1. User opens app first time
2. Sees welcome screen
3. Taps "Get Started"
4. Sees permission cards
5. Taps "Grant Permission" for each
6. System dialogs appear
7. User grants permissions
8. "Continue" button enables
9. Taps "Continue"
10. Enters main app

**Background Operation**:
1. Notification arrives on device
2. EDGES captures it in background
3. Sends to Gemini AI
4. AI analyzes and decides
5. If high confidence: Auto-schedules
6. If medium confidence: Shows card in app
7. User opens app and sees suggestion
8. User takes action

