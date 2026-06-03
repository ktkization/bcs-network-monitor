# BCS Network Monitor — Frontend

React single-page application for monitoring network devices. Provides a device list with stale indicators, a registration form, and a detail page with status report history and submission.

## Prerequisites

- **Node.js** 18 or later

## Project Structure

```
src/
├── api/              Axios client and device API functions
│   ├── client.ts       Axios instance (baseURL: localhost:8080/api)
│   ├── devices.ts      registerDevice, fetchDevices, fetchDeviceDetail, submitStatusReport
│   └── devices.test.ts API layer tests
├── components/       Reusable components
│   ├── StatusBadge.tsx  Colored badge for ONLINE/OFFLINE/DEGRADED
│   ├── StaleIndicator.tsx  Warning badge when device is stale
│   └── ui/              shadcn/ui primitives (Button, Table, Card, Dialog, etc.)
├── lib/              Utility libraries
│   └── utils.ts         
├── pages/            Route-level pages
│   ├── DeviceListPage.tsx          Table of all devices with status and stale indicators
│   ├── DeviceListPage.test.tsx     Component tests
│   ├── RegisterDevicePage.tsx      Form to register a new device
│   ├── RegisterDevicePage.test.tsx Component tests
│   ├── DeviceDetailPage.tsx        Device info, report history, submit report dialog
│   └── DeviceDetailPage.test.tsx   Component tests
├── types/            TypeScript interfaces
│   └── index.ts           DeviceListItem, DeviceDetail, StatusReport, etc.
└── utils/            Helpers
    ├── stale.ts           formatDate, statusColor (stale boolean comes from backend API)
    └── stale.test.ts      Utility tests
```

## Setup

```bash
npm install
```

## Running

**Development server:**

```bash
npm run dev
```

Opens at **http://localhost:5173**. The backend must be running on `http://localhost:8080` for API calls to work.

**Production build:**

```bash
npm run build
```

Output is written to `dist/`. Serve with any static file server.

**Preview production build:**

```bash
npm run preview
```

**Lint:**

```bash
npm run lint
```

**Tests:**

```bash
npm test
```

Runs all Vitest tests (unit, component, and API layer tests).

## Routes

| Path                 | Page                | Description                                  |
|----------------------|---------------------|----------------------------------------------|
| `/`                  | Device List         | Default redirect to `/devices`               |
| `/devices`           | Device List         | Table of all devices with status and stale   |
| `/devices/register`  | Register Device     | Form to register a new network device        |
| `/devices/:id`       | Device Detail       | Device info card, recent reports, submit     |

## Features

- **Device list** — sortable table showing name, type, hostname, location, current status, last report timestamp, and stale indicator
- **Device registration** — form with validation for all required fields (uniqueId, name, deviceType, hostname, location) plus optional IP address
- **Device detail** — info card with all device metadata, current status badge, stale indicator, and the 20 most recent status reports
- **Submit status report** — dialog on the detail page with status dropdown (ONLINE, OFFLINE, DEGRADED) and optional message

## Tech Stack

| Library              | Version | Purpose                      |
|----------------------|---------|------------------------------|
| React                | 19      | UI framework                 |
| Vite                 | 8       | Build tool and dev server    |
| TypeScript           | 6       | Type safety                  |
| Tailwind CSS         | 4       | Utility-first styling        |
| shadcn/ui            | 4       | Pre-built UI components      |
| Axios                | 1.x     | HTTP client                  |
| React Router DOM     | 7       | Client-side routing          |
| Vitest               | 4       | Test runner                  |
| @testing-library/react | 16    | React component testing      |
| jsdom                | 26      | Browser environment for tests|
