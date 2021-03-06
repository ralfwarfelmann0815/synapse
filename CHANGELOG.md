# Release Notes

## 0.7.3
* Fix bug in KinesisShardIterator. Read next record when there is no data near the part of the shard pointed to by the ShardIterator. See https://docs.aws.amazon.com/streams/latest/dev/troubleshooting-consumers.html#getrecords-returns-empty.

## 0.7.2
* Fix name for InMemoryMessageSenderFactory in InMemoryTestConfiguration to override kinesisSenderEndpointFactory in autowiring 

## 0.7.1
* Update dependency to edison-aws 0.4.1 which works with AWS SDK preview 10

## 0.7.0
* Updated to AWS SDK preview 10
* Added support for AWS SQS with SqsMessageSender and SqsMessageQueueReceiverEndpoint
* New KinesisMessageLogReader for low-level polling of Kinesis messages.
* Refactored interfaces for `EventSource` and `MessageLogReceiverEndpoint`. The interfaces now immediately return a 
CompletedFuture instead of blocking forever.
* Add StartFrom AT-POSITION to access entries in the stream directly

## 0.6.13
* Add ConditionalOnMissingBean to ObjectMapper in SynapseAutoConfiguration

## 0.6.12
* Fixes bug that don't create a new message when retry with a corrupt byte buffer 

## 0.6.11
* Adds message traces for sender- and receiver endpoints to Edison µServices. 

## 0.6.9
* Fixes bug that SnapshotAutoConfiguration is not injecting the ApplicationEventPublisher into SnapshotMessageStore
  instances created by the SnapshotMessageStoreFactory.
* Log message meta data when put to kinesis failed

## 0.6.8
* Fixes bug in MessageReceiverEndpointInfoProvider resulting in a broken presentation of status details. 
* Disabling of synapse-edison is now more consistent. The different Health Indicators can now be disabled using the
  following properties:
  - `StartUpHealthIndicator`: synapse.edison.health.startup.enabled=false
  - `SnapshotReaderHealthIndicator`: synapse.edison.health.snapshotreader.enabled=false
  - `MessageReceiverEndpointHealthIndicator`: synapse.edison.health.messagereceiver.enabled=false
  
## 0.6.7
* Introduced interfaces for MessageEndpoint, MessageReceiverEndpoint, MessageLogReceiverEndpoint and 
  MessageQueueReceiverEndpoints.
* Refactored the creation of EventSources: The associated MessageLogReceiverEndpoints are now registered in the 
  ApplicationContext, so it is possible to inject these into other beans.
  
## 0.6.6
* Removed `EnableEventSource#builder()` and replaced it by an 
  auto-configuration of the new `MessageSenderEndpointFactory` and 
  `MessageLogReceiverEndpointFactory` instances, together with the (also new) 
  general-purpose `DefaultEventSource` implementation that is replacing the
  different other `EventSource` implementations.
* Simplified the configuration of in-memory implementations of the different endpoints for
  testing purposes. It is now possible to just add `@ImportAutoConfiguration(InMemoryTestConfiguration.class)` to
  your test configuration to do this.
* Removed `Predicate` from `EventSource` and `MessageLogReceiverEndpoint` interfaces and
  replaced it by `consumeUntil()` methods taking an `Instant`as a parameter to stop
  message retrieval at a specified timestamp.
* Removed `durationBehind` from channel- and shard-positions.
* Introduced type `ChannelDurationBehind` that is used in notifications to announce the duration that consumers are 
  behind of the channel head position.   
* Renamed `EventSourcingHealthIndicator` to `MessageReceiverEndpointHealthIndicator`
* Introduced `SnapshotReaderHealthIndicator`  
* Renamed `EventSourcingStatusDetailIndicator` to `MessageReceiverStatusDetailIndicator`
* Introduced `SnapshotStatusDetailIndicator`
* Refactored eventsource notifications and separated them into `SnapshotReaderNotification` and 
  `MessageReceiverNotification`.

## 0.6.5
* Added `StartupHealthIndicator` that is unhealthy until all EventSources are (almost) up to date.
* Added possibility to consume MessageLogs from timestamp

## 0.6.4
* Fixed problem that the KinesisShardIterator will not recover after an exception is thrown
* Introduced MessageEndpointConfigurer used to register MessageInterceptors at MessageSender- and/or
  MessageReceiverEndpoints.
* Added MessageFilter as a special implementation of a MessageInterceptor that is used to filter messages depending
  on a Predicate

## 0.6.3
* Using key-value pairs in (some) log messages

## 0.6.2
* Speedup snapshot creation and log progress

## 0.6.1
* Introduce special SnapshotEventSourceNotification that additionally holds the timestamp of snapshot creation.

## 0.5.0 Major Refactoring
* Renamed project to OTTO Synapse:
  * eventsourcing-core -> synapse-core
  * eventsourcing-aws -> synapse-aws
  * eventsourcing-edison-integration -> synapse-edison
* Renamed packages to de.otto.synapse.*
* Renamed properties to synapse.*
* Introduced eventsourcing-aws and removed aws-specific parts from eventsourcing-/synapse-core
* Renamed Event to Message and removed EventBody

## 0.4.8
* **[eventsourcing-edison-integration]** Add health indicator for stream state.
 This means that a service goes unhealthy if a stream is in a not-recoverable state.
* **[eventsourcing-core]** Fix retry policy in `KinesisShardIterator` to retry
 also on connection errors, not only on throughput exceed errors. 

## 0.4.7
Remove `Clock` bean as this is required only for tests.
Don't set StatusDetailIndicator to warn when kinesis consumer has finished.

## 0.4.6
Add `Clock` bean that is required by `EventSourcingStatusDetailIndicator` 

## 0.4.5
* New sub project `eventsourcing-edison-integration`. This project contains a `StatusDetailIndicator` that provides 
StatusDetail information for each EventSource.

## 0.4.4
* Also publish EventSourceNotification Application Events for Kinesis and InMemory EventSources

## 0.4.3
* Remove further ChronicleMap closed errors on shutdown. 

## 0.4.2
* Prevent ChronicleMap closed errors on shutdown.

## 0.4.1
* Add `sendEvents` method to `EventSender` interface.

## 0.4.0
* Breaking changes:
  * Change signature of `KinesisEventSender.sendEvents`. Send events takes a list of `EventBody` now
  * Split Event class into `Event` and `EventBody`
  * `CompactionService` now requires a `StateRepository` with name `compactionStateRepository` 
* InMemory EventSender and EventSource for testing


## 0.3.0
* Events with "null"-payload will delete the entry.
* Remove client side encryption because kinesis now supports server side encryption.

## 0.2.2
* `SnapshotReadService` now allows to set a local snapshot file to read from instead retrieving it from AWS S3.   
This functionality was moved from `SnapshotEventSource` and also works for a `CompactingKinesisEventSource` now.

## 0.2.1
* Provide `EncryptedOrPlainJsonTextEncryptor` that checks whether data is plain json or is encrypted.

## 0.2.0
* Add option to send unencrypted events to `KinesisEventSender.sendEvent(String, Object, boolean)` 
and `KinesisEventSender.sendEvents(Map<String,Object>, boolean)`

## 0.1.11
* Fix statistics
* Clear state repository after compaction job

## 0.1.1
* Released to keep things stable

## 0.1.0
**Initial Release**
