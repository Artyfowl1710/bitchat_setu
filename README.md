# Setu — Adaptive Device-to-Device Mesh Communication System

<p align="center">
  <strong>Offline-First Communication Through Nearby Devices</strong>
</p>

<p align="center">
  A mini project focused on enabling communication between nearby Android devices when conventional internet or cellular connectivity is unavailable.
</p>

---

## Overview

**Setu** is an offline-first, device-to-device communication system designed to help messages and other data move across nearby devices even when conventional network infrastructure is unavailable.

The project builds upon the open-source Bitchat Android codebase and explores adaptive communication techniques for low-connectivity environments, local dead zones, and emergency situations.

Instead of requiring every device to connect directly to the internet, Setu aims to use nearby devices as intermediate relay nodes. A message can be forwarded from one device to another until it reaches its intended destination, subject to device availability, supported transport mechanisms, and network topology.

The project also explores lightweight voice-note transmission, opportunistic higher-bandwidth communication, and cooperative emergency-alert forwarding.

## Problem Statement

Conventional messaging applications generally depend on internet connectivity or cellular infrastructure. In rural areas, disaster-affected regions, underground locations, and network dead zones, this dependency can prevent users from communicating.

Setu investigates an alternative approach in which nearby smartphones cooperate to relay messages across a local device-to-device network.

The objective is to reduce dependence on continuous internet connectivity for local communication and provide a foundation for emergency messaging in infrastructure-constrained environments.

## Objectives

- Enable offline communication between nearby Android devices.
- Support multi-hop message forwarding through intermediate devices.
- Explore compact voice-note encoding for bandwidth-constrained communication.
- Investigate higher-bandwidth transfer options when supported by compatible devices.
- Design a cooperative SOS relay mechanism for emergency communication.
- Evaluate message delivery, transfer performance, compatibility, and reliability under different connectivity conditions.

## Key Features

### 1. Bluetooth Low Energy (BLE) Communication

Uses Bluetooth Low Energy as the baseline short-range communication mechanism for discovering nearby devices and exchanging data, subject to the underlying implementation.

### 2. Multi-Hop Message Relay

A message can be forwarded through intermediate devices when the sender and recipient are outside direct communication range.

The intended relay workflow includes message identification, duplicate detection, hop limits, and delivery-status handling.

### 3. Voice-Note Optimization

The project investigates using the **AMR-NB codec at 4.75 kbps** to reduce voice-note payload size for low-bandwidth communication.

The target is to achieve at least a fourfold reduction in encoded file size compared with a 20 kbps AAC baseline, subject to audio quality and measured results.

### 4. Adaptive Transport

The project explores **Wi-Fi Aware** as an optional higher-bandwidth communication path for larger payloads on compatible devices, while retaining BLE as the baseline transport.

Actual transport selection depends on device capabilities and implementation support.

### 5. Cooperative SOS Relay

The proposed emergency workflow uses an `SOS_RELAY` message that can propagate across intermediate devices.

Message identifiers, TTL (Time to Live), and duplicate detection help control repeated forwarding. A relay device that regains internet or cellular connectivity can forward the SOS to an external gateway, provided the gateway integration is configured and available.

**Important:** SOS delivery is not guaranteed. It depends on relay availability, connectivity, gateway configuration, and successful forwarding.

## System Architecture

The intended communication workflow is:

```text
Sender Device
     |
     v
BLE Device Discovery
     |
     v
Message / Voice Note Preparation
     |
     v
Nearby Relay Device
     |
     v
Multi-Hop Forwarding
     |
     v
Destination Device
```

For emergency communication, the intended extension is:

```text
Emergency User
      |
      v
SOS Alert Creation
      |
      v
Nearby Device / Relay Node
      |
      v
Additional Relay Nodes
      |
      v
Connectivity-Enabled Gateway
      |
      v
External Alert Service
```

These diagrams represent the intended workflows. Actual behavior depends on the features implemented in the current codebase.

## Technology Stack

The project is based on the Bitchat Android codebase and investigates the following technologies:

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| Android SDK | Application and device integration |
| Bluetooth Low Energy (BLE) | Nearby device communication |
| Wi-Fi Aware | Optional higher-bandwidth peer communication |
| AMR-NB | Low-bitrate voice-note encoding |
| Android networking APIs | Device discovery and data transfer |
| External gateway integration | Forwarding emergency alerts when connectivity is available |

The exact libraries and supported Android versions depend on the implementation in this repository.

## Getting Started

### Prerequisites

- A computer with Android development tools installed.
- Android Studio compatible with the project's Gradle and Android Gradle Plugin versions.
- A compatible JDK as required by the project.
- Android devices with the required Bluetooth and other supported hardware capabilities.
- USB debugging enabled for physical-device testing, if needed.

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/Artyfowl1710/bitchat_setu.git
   ```

2. Enter the project directory:

   ```bash
   cd bitchat_setu
   ```

3. Open the project in Android Studio.

4. Allow Gradle synchronization to complete.

5. Review the project's build configuration, SDK requirements, and any required local configuration.

6. Connect a compatible Android device or configure an emulator.

7. Build and run the application using Android Studio.

If you prefer the terminal, use the Gradle wrapper commands supported by the repository. For example, on Windows:

```powershell
.\gradlew.bat assembleDebug
```

On Linux or macOS:

```bash
./gradlew assembleDebug
```

These commands assume the repository contains a working Gradle wrapper and a compatible Android application module.

## Testing and Evaluation

The project can be evaluated using the following test scenarios:

| Test Case | Purpose |
|---|---|
| Nearby device discovery | Verify that compatible devices can discover one another |
| Direct message transfer | Check message transmission between two devices |
| Multi-hop forwarding | Verify forwarding through intermediate devices |
| Voice-note transfer | Check voice-note encoding, transfer, and playback |
| Codec comparison | Compare encoded file sizes against the selected AAC baseline |
| Wi-Fi Aware compatibility | Verify behavior on supported and unsupported devices |
| SOS relay | Test emergency-alert propagation and gateway forwarding |
| Failure handling | Observe behavior during disconnection, relay failure, and unavailable connectivity |
| Performance evaluation | Measure delivery success, transfer time, and battery consumption |

Record actual measurements and device configurations when reporting results. Features that have not been implemented or tested should be reported as pending rather than completed.

## Project Scope

### Included

- Offline-first device-to-device communication.
- BLE-based peer communication.
- Multi-hop relay and message-forwarding evaluation.
- Voice-note optimization research.
- Optional higher-bandwidth transport investigation.
- Cooperative SOS relay and gateway integration.
- Functional and performance testing.

### Outside the Current Scope

- Live voice and video calling.
- Replacing the underlying encryption protocol or changing non-SOS message security.
- Making LoRa or Meshtastic hardware a requirement.
- Guaranteeing emergency delivery when no relay or external connectivity is available.

## Limitations

- BLE communication range and reliability depend on the environment and device hardware.
- Android background-execution and Bluetooth permission restrictions can affect communication.
- Wi-Fi Aware availability varies across devices and Android implementations.
- Voice compression may involve trade-offs between file size and audio quality.
- SOS gateway delivery requires a suitable gateway and available external connectivity.
- Multi-hop delivery depends on the availability and cooperation of intermediate devices.

## Future Enhancements

- Improved adaptive routing and relay selection.
- More extensive testing across Android manufacturers.
- Better battery-aware forwarding strategies.
- Improved delivery acknowledgements and failure recovery.
- Further voice-codec optimization and objective audio-quality evaluation.
- More robust emergency gateway integration and delivery-status reporting.

## Team

**Project:** Adaptive Device-to-Device Mesh Communication System (Setu)

**Institution:** Sardar Patel Institute of Technology, Mumbai

**Department:** Computer Science and Engineering

**Project Guide:** Prof. Nikahat Mulla

**Team Members:**
- Kaivalya Sunil Sonawane
- Mihir Harshad Khatri
- Aniket Patil

## Acknowledgements

Setu builds upon the open-source Bitchat Android project. The original project's implementation and documentation should be consulted for inherited functionality, build requirements, and applicable licensing terms.

- **Setu repository:** https://github.com/Artyfowl1710/bitchat_setu
- **Original Bitchat Android repository:** https://github.com/permissionlesstech/bitchat-android

Please retain the original project's copyright notices and license files, and ensure that modifications and redistribution comply with its applicable license.

## License

Refer to the repository's existing `LICENSE` file and the original Bitchat Android project's license before redistributing this code. Do not assume that the project has a new or different license unless one has been explicitly established.

---

<p align="center">
  <strong>Setu — Connecting People Through Nearby Devices</strong>
</p>
