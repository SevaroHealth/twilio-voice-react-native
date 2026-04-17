import type { Constants } from '../constants';
import type { NativeAudioDevicesUpdatedEvent } from './AudioDevice';
import type { NativeCallInviteInfo } from './CallInvite';
import type { NativeErrorEvent } from './Error';

export interface NativeRegisteredEvent {
  type: Constants.VoiceEventRegistered;
}

export interface NativeUnregisteredEvent {
  type: Constants.VoiceEventUnregistered;
}

export interface NativeDeviceTokenUpdatedEvent {
  type: Constants.VoiceEventDeviceTokenUpdated;
}

export interface NativeDeviceTokenInvalidatedEvent {
  type: Constants.VoiceEventDeviceTokenInvalidated;
}

export interface NativeCallInviteIncomingEvent {
  [Constants.VoiceEventType]: Constants.VoiceEventTypeValueIncomingCallInvite;
  callInvite: NativeCallInviteInfo;
}

export type NativeVoiceEvent =
  | NativeAudioDevicesUpdatedEvent
  | NativeCallInviteIncomingEvent
  | NativeErrorEvent
  | NativeRegisteredEvent
  | NativeUnregisteredEvent
  | NativeDeviceTokenUpdatedEvent
  | NativeDeviceTokenInvalidatedEvent;

export type NativeVoiceEventType =
  | Constants.VoiceEventAudioDevicesUpdated
  | Constants.VoiceEventTypeValueIncomingCallInvite
  | Constants.VoiceEventError
  | Constants.VoiceEventRegistered
  | Constants.VoiceEventUnregistered
  | Constants.VoiceEventDeviceTokenUpdated
  | Constants.VoiceEventDeviceTokenInvalidated;
