// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: challenger.proto

package grpc.modules;

/**
 * Protobuf type {@code Challenger.Batch}
 */
public  final class Batch extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:Challenger.Batch)
    BatchOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Batch.newBuilder() to construct.
  private Batch(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Batch() {
    lookupSymbols_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    events_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Batch();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Batch(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 8: {

            seqId_ = input.readInt64();
            break;
          }
          case 16: {

            last_ = input.readBool();
            break;
          }
          case 26: {
            java.lang.String s = input.readStringRequireUtf8();
            if (!((mutable_bitField0_ & 0x00000001) != 0)) {
              lookupSymbols_ = new com.google.protobuf.LazyStringArrayList();
              mutable_bitField0_ |= 0x00000001;
            }
            lookupSymbols_.add(s);
            break;
          }
          case 34: {
            if (!((mutable_bitField0_ & 0x00000002) != 0)) {
              events_ = new java.util.ArrayList<grpc.modules.Event>();
              mutable_bitField0_ |= 0x00000002;
            }
            events_.add(
                input.readMessage(grpc.modules.Event.parser(), extensionRegistry));
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000001) != 0)) {
        lookupSymbols_ = lookupSymbols_.getUnmodifiableView();
      }
      if (((mutable_bitField0_ & 0x00000002) != 0)) {
        events_ = java.util.Collections.unmodifiableList(events_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return grpc.modules.ChallengerProto.internal_static_Challenger_Batch_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return grpc.modules.ChallengerProto.internal_static_Challenger_Batch_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            grpc.modules.Batch.class, grpc.modules.Batch.Builder.class);
  }

  public static final int SEQ_ID_FIELD_NUMBER = 1;
  private long seqId_;
  /**
   * <code>int64 seq_id = 1;</code>
   * @return The seqId.
   */
  public long getSeqId() {
    return seqId_;
  }

  public static final int LAST_FIELD_NUMBER = 2;
  private boolean last_;
  /**
   * <code>bool last = 2;</code>
   * @return The last.
   */
  public boolean getLast() {
    return last_;
  }

  public static final int LOOKUP_SYMBOLS_FIELD_NUMBER = 3;
  private com.google.protobuf.LazyStringList lookupSymbols_;
  /**
   * <code>repeated string lookup_symbols = 3;</code>
   * @return A list containing the lookupSymbols.
   */
  public com.google.protobuf.ProtocolStringList
      getLookupSymbolsList() {
    return lookupSymbols_;
  }
  /**
   * <code>repeated string lookup_symbols = 3;</code>
   * @return The count of lookupSymbols.
   */
  public int getLookupSymbolsCount() {
    return lookupSymbols_.size();
  }
  /**
   * <code>repeated string lookup_symbols = 3;</code>
   * @param index The index of the element to return.
   * @return The lookupSymbols at the given index.
   */
  public java.lang.String getLookupSymbols(int index) {
    return lookupSymbols_.get(index);
  }
  /**
   * <code>repeated string lookup_symbols = 3;</code>
   * @param index The index of the value to return.
   * @return The bytes of the lookupSymbols at the given index.
   */
  public com.google.protobuf.ByteString
      getLookupSymbolsBytes(int index) {
    return lookupSymbols_.getByteString(index);
  }

  public static final int EVENTS_FIELD_NUMBER = 4;
  private java.util.List<grpc.modules.Event> events_;
  /**
   * <code>repeated .Challenger.Event events = 4;</code>
   */
  public java.util.List<grpc.modules.Event> getEventsList() {
    return events_;
  }
  /**
   * <code>repeated .Challenger.Event events = 4;</code>
   */
  public java.util.List<? extends grpc.modules.EventOrBuilder> 
      getEventsOrBuilderList() {
    return events_;
  }
  /**
   * <code>repeated .Challenger.Event events = 4;</code>
   */
  public int getEventsCount() {
    return events_.size();
  }
  /**
   * <code>repeated .Challenger.Event events = 4;</code>
   */
  public grpc.modules.Event getEvents(int index) {
    return events_.get(index);
  }
  /**
   * <code>repeated .Challenger.Event events = 4;</code>
   */
  public grpc.modules.EventOrBuilder getEventsOrBuilder(
      int index) {
    return events_.get(index);
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (seqId_ != 0L) {
      output.writeInt64(1, seqId_);
    }
    if (last_ != false) {
      output.writeBool(2, last_);
    }
    for (int i = 0; i < lookupSymbols_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, lookupSymbols_.getRaw(i));
    }
    for (int i = 0; i < events_.size(); i++) {
      output.writeMessage(4, events_.get(i));
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (seqId_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(1, seqId_);
    }
    if (last_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(2, last_);
    }
    {
      int dataSize = 0;
      for (int i = 0; i < lookupSymbols_.size(); i++) {
        dataSize += computeStringSizeNoTag(lookupSymbols_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getLookupSymbolsList().size();
    }
    for (int i = 0; i < events_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(4, events_.get(i));
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof grpc.modules.Batch)) {
      return super.equals(obj);
    }
    grpc.modules.Batch other = (grpc.modules.Batch) obj;

    if (getSeqId()
        != other.getSeqId()) return false;
    if (getLast()
        != other.getLast()) return false;
    if (!getLookupSymbolsList()
        .equals(other.getLookupSymbolsList())) return false;
    if (!getEventsList()
        .equals(other.getEventsList())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + SEQ_ID_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getSeqId());
    hash = (37 * hash) + LAST_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getLast());
    if (getLookupSymbolsCount() > 0) {
      hash = (37 * hash) + LOOKUP_SYMBOLS_FIELD_NUMBER;
      hash = (53 * hash) + getLookupSymbolsList().hashCode();
    }
    if (getEventsCount() > 0) {
      hash = (37 * hash) + EVENTS_FIELD_NUMBER;
      hash = (53 * hash) + getEventsList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static grpc.modules.Batch parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static grpc.modules.Batch parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static grpc.modules.Batch parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static grpc.modules.Batch parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static grpc.modules.Batch parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static grpc.modules.Batch parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static grpc.modules.Batch parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static grpc.modules.Batch parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static grpc.modules.Batch parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static grpc.modules.Batch parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static grpc.modules.Batch parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static grpc.modules.Batch parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(grpc.modules.Batch prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code Challenger.Batch}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:Challenger.Batch)
      grpc.modules.BatchOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return grpc.modules.ChallengerProto.internal_static_Challenger_Batch_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return grpc.modules.ChallengerProto.internal_static_Challenger_Batch_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              grpc.modules.Batch.class, grpc.modules.Batch.Builder.class);
    }

    // Construct using grpc.modules.Batch.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
        getEventsFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      seqId_ = 0L;

      last_ = false;

      lookupSymbols_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000001);
      if (eventsBuilder_ == null) {
        events_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000002);
      } else {
        eventsBuilder_.clear();
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return grpc.modules.ChallengerProto.internal_static_Challenger_Batch_descriptor;
    }

    @java.lang.Override
    public grpc.modules.Batch getDefaultInstanceForType() {
      return grpc.modules.Batch.getDefaultInstance();
    }

    @java.lang.Override
    public grpc.modules.Batch build() {
      grpc.modules.Batch result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public grpc.modules.Batch buildPartial() {
      grpc.modules.Batch result = new grpc.modules.Batch(this);
      int from_bitField0_ = bitField0_;
      result.seqId_ = seqId_;
      result.last_ = last_;
      if (((bitField0_ & 0x00000001) != 0)) {
        lookupSymbols_ = lookupSymbols_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000001);
      }
      result.lookupSymbols_ = lookupSymbols_;
      if (eventsBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0)) {
          events_ = java.util.Collections.unmodifiableList(events_);
          bitField0_ = (bitField0_ & ~0x00000002);
        }
        result.events_ = events_;
      } else {
        result.events_ = eventsBuilder_.build();
      }
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof grpc.modules.Batch) {
        return mergeFrom((grpc.modules.Batch)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(grpc.modules.Batch other) {
      if (other == grpc.modules.Batch.getDefaultInstance()) return this;
      if (other.getSeqId() != 0L) {
        setSeqId(other.getSeqId());
      }
      if (other.getLast() != false) {
        setLast(other.getLast());
      }
      if (!other.lookupSymbols_.isEmpty()) {
        if (lookupSymbols_.isEmpty()) {
          lookupSymbols_ = other.lookupSymbols_;
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          ensureLookupSymbolsIsMutable();
          lookupSymbols_.addAll(other.lookupSymbols_);
        }
        onChanged();
      }
      if (eventsBuilder_ == null) {
        if (!other.events_.isEmpty()) {
          if (events_.isEmpty()) {
            events_ = other.events_;
            bitField0_ = (bitField0_ & ~0x00000002);
          } else {
            ensureEventsIsMutable();
            events_.addAll(other.events_);
          }
          onChanged();
        }
      } else {
        if (!other.events_.isEmpty()) {
          if (eventsBuilder_.isEmpty()) {
            eventsBuilder_.dispose();
            eventsBuilder_ = null;
            events_ = other.events_;
            bitField0_ = (bitField0_ & ~0x00000002);
            eventsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getEventsFieldBuilder() : null;
          } else {
            eventsBuilder_.addAllMessages(other.events_);
          }
        }
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      grpc.modules.Batch parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (grpc.modules.Batch) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private long seqId_ ;
    /**
     * <code>int64 seq_id = 1;</code>
     * @return The seqId.
     */
    public long getSeqId() {
      return seqId_;
    }
    /**
     * <code>int64 seq_id = 1;</code>
     * @param value The seqId to set.
     * @return This builder for chaining.
     */
    public Builder setSeqId(long value) {
      
      seqId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int64 seq_id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearSeqId() {
      
      seqId_ = 0L;
      onChanged();
      return this;
    }

    private boolean last_ ;
    /**
     * <code>bool last = 2;</code>
     * @return The last.
     */
    public boolean getLast() {
      return last_;
    }
    /**
     * <code>bool last = 2;</code>
     * @param value The last to set.
     * @return This builder for chaining.
     */
    public Builder setLast(boolean value) {
      
      last_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>bool last = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearLast() {
      
      last_ = false;
      onChanged();
      return this;
    }

    private com.google.protobuf.LazyStringList lookupSymbols_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    private void ensureLookupSymbolsIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        lookupSymbols_ = new com.google.protobuf.LazyStringArrayList(lookupSymbols_);
        bitField0_ |= 0x00000001;
       }
    }
    /**
     * <code>repeated string lookup_symbols = 3;</code>
     * @return A list containing the lookupSymbols.
     */
    public com.google.protobuf.ProtocolStringList
        getLookupSymbolsList() {
      return lookupSymbols_.getUnmodifiableView();
    }
    /**
     * <code>repeated string lookup_symbols = 3;</code>
     * @return The count of lookupSymbols.
     */
    public int getLookupSymbolsCount() {
      return lookupSymbols_.size();
    }
    /**
     * <code>repeated string lookup_symbols = 3;</code>
     * @param index The index of the element to return.
     * @return The lookupSymbols at the given index.
     */
    public java.lang.String getLookupSymbols(int index) {
      return lookupSymbols_.get(index);
    }
    /**
     * <code>repeated string lookup_symbols = 3;</code>
     * @param index The index of the value to return.
     * @return The bytes of the lookupSymbols at the given index.
     */
    public com.google.protobuf.ByteString
        getLookupSymbolsBytes(int index) {
      return lookupSymbols_.getByteString(index);
    }
    /**
     * <code>repeated string lookup_symbols = 3;</code>
     * @param index The index to set the value at.
     * @param value The lookupSymbols to set.
     * @return This builder for chaining.
     */
    public Builder setLookupSymbols(
        int index, java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureLookupSymbolsIsMutable();
      lookupSymbols_.set(index, value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string lookup_symbols = 3;</code>
     * @param value The lookupSymbols to add.
     * @return This builder for chaining.
     */
    public Builder addLookupSymbols(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureLookupSymbolsIsMutable();
      lookupSymbols_.add(value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string lookup_symbols = 3;</code>
     * @param values The lookupSymbols to add.
     * @return This builder for chaining.
     */
    public Builder addAllLookupSymbols(
        java.lang.Iterable<java.lang.String> values) {
      ensureLookupSymbolsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, lookupSymbols_);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string lookup_symbols = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearLookupSymbols() {
      lookupSymbols_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string lookup_symbols = 3;</code>
     * @param value The bytes of the lookupSymbols to add.
     * @return This builder for chaining.
     */
    public Builder addLookupSymbolsBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      ensureLookupSymbolsIsMutable();
      lookupSymbols_.add(value);
      onChanged();
      return this;
    }

    private java.util.List<grpc.modules.Event> events_ =
      java.util.Collections.emptyList();
    private void ensureEventsIsMutable() {
      if (!((bitField0_ & 0x00000002) != 0)) {
        events_ = new java.util.ArrayList<grpc.modules.Event>(events_);
        bitField0_ |= 0x00000002;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        grpc.modules.Event, grpc.modules.Event.Builder, grpc.modules.EventOrBuilder> eventsBuilder_;

    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public java.util.List<grpc.modules.Event> getEventsList() {
      if (eventsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(events_);
      } else {
        return eventsBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public int getEventsCount() {
      if (eventsBuilder_ == null) {
        return events_.size();
      } else {
        return eventsBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public grpc.modules.Event getEvents(int index) {
      if (eventsBuilder_ == null) {
        return events_.get(index);
      } else {
        return eventsBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public Builder setEvents(
        int index, grpc.modules.Event value) {
      if (eventsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureEventsIsMutable();
        events_.set(index, value);
        onChanged();
      } else {
        eventsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public Builder setEvents(
        int index, grpc.modules.Event.Builder builderForValue) {
      if (eventsBuilder_ == null) {
        ensureEventsIsMutable();
        events_.set(index, builderForValue.build());
        onChanged();
      } else {
        eventsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public Builder addEvents(grpc.modules.Event value) {
      if (eventsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureEventsIsMutable();
        events_.add(value);
        onChanged();
      } else {
        eventsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public Builder addEvents(
        int index, grpc.modules.Event value) {
      if (eventsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureEventsIsMutable();
        events_.add(index, value);
        onChanged();
      } else {
        eventsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public Builder addEvents(
        grpc.modules.Event.Builder builderForValue) {
      if (eventsBuilder_ == null) {
        ensureEventsIsMutable();
        events_.add(builderForValue.build());
        onChanged();
      } else {
        eventsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public Builder addEvents(
        int index, grpc.modules.Event.Builder builderForValue) {
      if (eventsBuilder_ == null) {
        ensureEventsIsMutable();
        events_.add(index, builderForValue.build());
        onChanged();
      } else {
        eventsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public Builder addAllEvents(
        java.lang.Iterable<? extends grpc.modules.Event> values) {
      if (eventsBuilder_ == null) {
        ensureEventsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, events_);
        onChanged();
      } else {
        eventsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public Builder clearEvents() {
      if (eventsBuilder_ == null) {
        events_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
      } else {
        eventsBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public Builder removeEvents(int index) {
      if (eventsBuilder_ == null) {
        ensureEventsIsMutable();
        events_.remove(index);
        onChanged();
      } else {
        eventsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public grpc.modules.Event.Builder getEventsBuilder(
        int index) {
      return getEventsFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public grpc.modules.EventOrBuilder getEventsOrBuilder(
        int index) {
      if (eventsBuilder_ == null) {
        return events_.get(index);  } else {
        return eventsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public java.util.List<? extends grpc.modules.EventOrBuilder> 
         getEventsOrBuilderList() {
      if (eventsBuilder_ != null) {
        return eventsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(events_);
      }
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public grpc.modules.Event.Builder addEventsBuilder() {
      return getEventsFieldBuilder().addBuilder(
          grpc.modules.Event.getDefaultInstance());
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public grpc.modules.Event.Builder addEventsBuilder(
        int index) {
      return getEventsFieldBuilder().addBuilder(
          index, grpc.modules.Event.getDefaultInstance());
    }
    /**
     * <code>repeated .Challenger.Event events = 4;</code>
     */
    public java.util.List<grpc.modules.Event.Builder> 
         getEventsBuilderList() {
      return getEventsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        grpc.modules.Event, grpc.modules.Event.Builder, grpc.modules.EventOrBuilder> 
        getEventsFieldBuilder() {
      if (eventsBuilder_ == null) {
        eventsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            grpc.modules.Event, grpc.modules.Event.Builder, grpc.modules.EventOrBuilder>(
                events_,
                ((bitField0_ & 0x00000002) != 0),
                getParentForChildren(),
                isClean());
        events_ = null;
      }
      return eventsBuilder_;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:Challenger.Batch)
  }

  // @@protoc_insertion_point(class_scope:Challenger.Batch)
  private static final grpc.modules.Batch DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new grpc.modules.Batch();
  }

  public static grpc.modules.Batch getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Batch>
      PARSER = new com.google.protobuf.AbstractParser<Batch>() {
    @java.lang.Override
    public Batch parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Batch(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Batch> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Batch> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public grpc.modules.Batch getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

