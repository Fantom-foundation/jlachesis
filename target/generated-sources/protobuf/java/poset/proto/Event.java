// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: poset/event.proto

package poset.proto;

/**
 * Protobuf type {@code poset.proto.Event}
 */
public  final class Event extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:poset.proto.Event)
    EventOrBuilder {
  // Use Event.newBuilder() to construct.
  private Event(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Event() {
    round_ = 0L;
    lamportTimestamp_ = 0L;
    roundReceived_ = 0L;
    creator_ = "";
    hash_ = com.google.protobuf.ByteString.EMPTY;
    hex_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
  }
  private Event(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    int mutable_bitField0_ = 0;
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!input.skipField(tag)) {
              done = true;
            }
            break;
          }
          case 10: {
            poset.proto.EventMessage.Builder subBuilder = null;
            if (message_ != null) {
              subBuilder = message_.toBuilder();
            }
            message_ = input.readMessage(poset.proto.EventMessage.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(message_);
              message_ = subBuilder.buildPartial();
            }

            break;
          }
          case 16: {

            round_ = input.readInt64();
            break;
          }
          case 24: {

            lamportTimestamp_ = input.readInt64();
            break;
          }
          case 32: {

            roundReceived_ = input.readInt64();
            break;
          }
          case 42: {
            java.lang.String s = input.readStringRequireUtf8();

            creator_ = s;
            break;
          }
          case 50: {

            hash_ = input.readBytes();
            break;
          }
          case 58: {
            java.lang.String s = input.readStringRequireUtf8();

            hex_ = s;
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
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return poset.proto.PEvent.internal_static_poset_proto_Event_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return poset.proto.PEvent.internal_static_poset_proto_Event_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            poset.proto.Event.class, poset.proto.Event.Builder.class);
  }

  public static final int MESSAGE_FIELD_NUMBER = 1;
  private poset.proto.EventMessage message_;
  /**
   * <code>.poset.proto.EventMessage Message = 1;</code>
   */
  public boolean hasMessage() {
    return message_ != null;
  }
  /**
   * <code>.poset.proto.EventMessage Message = 1;</code>
   */
  public poset.proto.EventMessage getMessage() {
    return message_ == null ? poset.proto.EventMessage.getDefaultInstance() : message_;
  }
  /**
   * <code>.poset.proto.EventMessage Message = 1;</code>
   */
  public poset.proto.EventMessageOrBuilder getMessageOrBuilder() {
    return getMessage();
  }

  public static final int ROUND_FIELD_NUMBER = 2;
  private long round_;
  /**
   * <code>int64 Round = 2;</code>
   */
  public long getRound() {
    return round_;
  }

  public static final int LAMPORTTIMESTAMP_FIELD_NUMBER = 3;
  private long lamportTimestamp_;
  /**
   * <code>int64 LamportTimestamp = 3;</code>
   */
  public long getLamportTimestamp() {
    return lamportTimestamp_;
  }

  public static final int ROUNDRECEIVED_FIELD_NUMBER = 4;
  private long roundReceived_;
  /**
   * <code>int64 RoundReceived = 4;</code>
   */
  public long getRoundReceived() {
    return roundReceived_;
  }

  public static final int CREATOR_FIELD_NUMBER = 5;
  private volatile java.lang.Object creator_;
  /**
   * <code>string Creator = 5;</code>
   */
  public java.lang.String getCreator() {
    java.lang.Object ref = creator_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      creator_ = s;
      return s;
    }
  }
  /**
   * <code>string Creator = 5;</code>
   */
  public com.google.protobuf.ByteString
      getCreatorBytes() {
    java.lang.Object ref = creator_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      creator_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int HASH_FIELD_NUMBER = 6;
  private com.google.protobuf.ByteString hash_;
  /**
   * <code>bytes Hash = 6;</code>
   */
  public com.google.protobuf.ByteString getHash() {
    return hash_;
  }

  public static final int HEX_FIELD_NUMBER = 7;
  private volatile java.lang.Object hex_;
  /**
   * <code>string Hex = 7;</code>
   */
  public java.lang.String getHex() {
    java.lang.Object ref = hex_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      hex_ = s;
      return s;
    }
  }
  /**
   * <code>string Hex = 7;</code>
   */
  public com.google.protobuf.ByteString
      getHexBytes() {
    java.lang.Object ref = hex_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      hex_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (message_ != null) {
      output.writeMessage(1, getMessage());
    }
    if (round_ != 0L) {
      output.writeInt64(2, round_);
    }
    if (lamportTimestamp_ != 0L) {
      output.writeInt64(3, lamportTimestamp_);
    }
    if (roundReceived_ != 0L) {
      output.writeInt64(4, roundReceived_);
    }
    if (!getCreatorBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, creator_);
    }
    if (!hash_.isEmpty()) {
      output.writeBytes(6, hash_);
    }
    if (!getHexBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 7, hex_);
    }
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (message_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getMessage());
    }
    if (round_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(2, round_);
    }
    if (lamportTimestamp_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(3, lamportTimestamp_);
    }
    if (roundReceived_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(4, roundReceived_);
    }
    if (!getCreatorBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, creator_);
    }
    if (!hash_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(6, hash_);
    }
    if (!getHexBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(7, hex_);
    }
    memoizedSize = size;
    return size;
  }

  private static final long serialVersionUID = 0L;
  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof poset.proto.Event)) {
      return super.equals(obj);
    }
    poset.proto.Event other = (poset.proto.Event) obj;

    boolean result = true;
    result = result && (hasMessage() == other.hasMessage());
    if (hasMessage()) {
      result = result && getMessage()
          .equals(other.getMessage());
    }
    result = result && (getRound()
        == other.getRound());
    result = result && (getLamportTimestamp()
        == other.getLamportTimestamp());
    result = result && (getRoundReceived()
        == other.getRoundReceived());
    result = result && getCreator()
        .equals(other.getCreator());
    result = result && getHash()
        .equals(other.getHash());
    result = result && getHex()
        .equals(other.getHex());
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (hasMessage()) {
      hash = (37 * hash) + MESSAGE_FIELD_NUMBER;
      hash = (53 * hash) + getMessage().hashCode();
    }
    hash = (37 * hash) + ROUND_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getRound());
    hash = (37 * hash) + LAMPORTTIMESTAMP_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getLamportTimestamp());
    hash = (37 * hash) + ROUNDRECEIVED_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getRoundReceived());
    hash = (37 * hash) + CREATOR_FIELD_NUMBER;
    hash = (53 * hash) + getCreator().hashCode();
    hash = (37 * hash) + HASH_FIELD_NUMBER;
    hash = (53 * hash) + getHash().hashCode();
    hash = (37 * hash) + HEX_FIELD_NUMBER;
    hash = (53 * hash) + getHex().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static poset.proto.Event parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static poset.proto.Event parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static poset.proto.Event parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static poset.proto.Event parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static poset.proto.Event parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static poset.proto.Event parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static poset.proto.Event parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static poset.proto.Event parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static poset.proto.Event parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static poset.proto.Event parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(poset.proto.Event prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
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
   * Protobuf type {@code poset.proto.Event}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:poset.proto.Event)
      poset.proto.EventOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return poset.proto.PEvent.internal_static_poset_proto_Event_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return poset.proto.PEvent.internal_static_poset_proto_Event_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              poset.proto.Event.class, poset.proto.Event.Builder.class);
    }

    // Construct using poset.proto.Event.newBuilder()
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
      }
    }
    public Builder clear() {
      super.clear();
      if (messageBuilder_ == null) {
        message_ = null;
      } else {
        message_ = null;
        messageBuilder_ = null;
      }
      round_ = 0L;

      lamportTimestamp_ = 0L;

      roundReceived_ = 0L;

      creator_ = "";

      hash_ = com.google.protobuf.ByteString.EMPTY;

      hex_ = "";

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return poset.proto.PEvent.internal_static_poset_proto_Event_descriptor;
    }

    public poset.proto.Event getDefaultInstanceForType() {
      return poset.proto.Event.getDefaultInstance();
    }

    public poset.proto.Event build() {
      poset.proto.Event result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public poset.proto.Event buildPartial() {
      poset.proto.Event result = new poset.proto.Event(this);
      if (messageBuilder_ == null) {
        result.message_ = message_;
      } else {
        result.message_ = messageBuilder_.build();
      }
      result.round_ = round_;
      result.lamportTimestamp_ = lamportTimestamp_;
      result.roundReceived_ = roundReceived_;
      result.creator_ = creator_;
      result.hash_ = hash_;
      result.hex_ = hex_;
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.setField(field, value);
    }
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof poset.proto.Event) {
        return mergeFrom((poset.proto.Event)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(poset.proto.Event other) {
      if (other == poset.proto.Event.getDefaultInstance()) return this;
      if (other.hasMessage()) {
        mergeMessage(other.getMessage());
      }
      if (other.getRound() != 0L) {
        setRound(other.getRound());
      }
      if (other.getLamportTimestamp() != 0L) {
        setLamportTimestamp(other.getLamportTimestamp());
      }
      if (other.getRoundReceived() != 0L) {
        setRoundReceived(other.getRoundReceived());
      }
      if (!other.getCreator().isEmpty()) {
        creator_ = other.creator_;
        onChanged();
      }
      if (other.getHash() != com.google.protobuf.ByteString.EMPTY) {
        setHash(other.getHash());
      }
      if (!other.getHex().isEmpty()) {
        hex_ = other.hex_;
        onChanged();
      }
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      poset.proto.Event parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (poset.proto.Event) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private poset.proto.EventMessage message_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
        poset.proto.EventMessage, poset.proto.EventMessage.Builder, poset.proto.EventMessageOrBuilder> messageBuilder_;
    /**
     * <code>.poset.proto.EventMessage Message = 1;</code>
     */
    public boolean hasMessage() {
      return messageBuilder_ != null || message_ != null;
    }
    /**
     * <code>.poset.proto.EventMessage Message = 1;</code>
     */
    public poset.proto.EventMessage getMessage() {
      if (messageBuilder_ == null) {
        return message_ == null ? poset.proto.EventMessage.getDefaultInstance() : message_;
      } else {
        return messageBuilder_.getMessage();
      }
    }
    /**
     * <code>.poset.proto.EventMessage Message = 1;</code>
     */
    public Builder setMessage(poset.proto.EventMessage value) {
      if (messageBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        message_ = value;
        onChanged();
      } else {
        messageBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.poset.proto.EventMessage Message = 1;</code>
     */
    public Builder setMessage(
        poset.proto.EventMessage.Builder builderForValue) {
      if (messageBuilder_ == null) {
        message_ = builderForValue.build();
        onChanged();
      } else {
        messageBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.poset.proto.EventMessage Message = 1;</code>
     */
    public Builder mergeMessage(poset.proto.EventMessage value) {
      if (messageBuilder_ == null) {
        if (message_ != null) {
          message_ =
            poset.proto.EventMessage.newBuilder(message_).mergeFrom(value).buildPartial();
        } else {
          message_ = value;
        }
        onChanged();
      } else {
        messageBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.poset.proto.EventMessage Message = 1;</code>
     */
    public Builder clearMessage() {
      if (messageBuilder_ == null) {
        message_ = null;
        onChanged();
      } else {
        message_ = null;
        messageBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.poset.proto.EventMessage Message = 1;</code>
     */
    public poset.proto.EventMessage.Builder getMessageBuilder() {
      
      onChanged();
      return getMessageFieldBuilder().getBuilder();
    }
    /**
     * <code>.poset.proto.EventMessage Message = 1;</code>
     */
    public poset.proto.EventMessageOrBuilder getMessageOrBuilder() {
      if (messageBuilder_ != null) {
        return messageBuilder_.getMessageOrBuilder();
      } else {
        return message_ == null ?
            poset.proto.EventMessage.getDefaultInstance() : message_;
      }
    }
    /**
     * <code>.poset.proto.EventMessage Message = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        poset.proto.EventMessage, poset.proto.EventMessage.Builder, poset.proto.EventMessageOrBuilder> 
        getMessageFieldBuilder() {
      if (messageBuilder_ == null) {
        messageBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            poset.proto.EventMessage, poset.proto.EventMessage.Builder, poset.proto.EventMessageOrBuilder>(
                getMessage(),
                getParentForChildren(),
                isClean());
        message_ = null;
      }
      return messageBuilder_;
    }

    private long round_ ;
    /**
     * <code>int64 Round = 2;</code>
     */
    public long getRound() {
      return round_;
    }
    /**
     * <code>int64 Round = 2;</code>
     */
    public Builder setRound(long value) {
      
      round_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int64 Round = 2;</code>
     */
    public Builder clearRound() {
      
      round_ = 0L;
      onChanged();
      return this;
    }

    private long lamportTimestamp_ ;
    /**
     * <code>int64 LamportTimestamp = 3;</code>
     */
    public long getLamportTimestamp() {
      return lamportTimestamp_;
    }
    /**
     * <code>int64 LamportTimestamp = 3;</code>
     */
    public Builder setLamportTimestamp(long value) {
      
      lamportTimestamp_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int64 LamportTimestamp = 3;</code>
     */
    public Builder clearLamportTimestamp() {
      
      lamportTimestamp_ = 0L;
      onChanged();
      return this;
    }

    private long roundReceived_ ;
    /**
     * <code>int64 RoundReceived = 4;</code>
     */
    public long getRoundReceived() {
      return roundReceived_;
    }
    /**
     * <code>int64 RoundReceived = 4;</code>
     */
    public Builder setRoundReceived(long value) {
      
      roundReceived_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int64 RoundReceived = 4;</code>
     */
    public Builder clearRoundReceived() {
      
      roundReceived_ = 0L;
      onChanged();
      return this;
    }

    private java.lang.Object creator_ = "";
    /**
     * <code>string Creator = 5;</code>
     */
    public java.lang.String getCreator() {
      java.lang.Object ref = creator_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        creator_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string Creator = 5;</code>
     */
    public com.google.protobuf.ByteString
        getCreatorBytes() {
      java.lang.Object ref = creator_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        creator_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string Creator = 5;</code>
     */
    public Builder setCreator(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      creator_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string Creator = 5;</code>
     */
    public Builder clearCreator() {
      
      creator_ = getDefaultInstance().getCreator();
      onChanged();
      return this;
    }
    /**
     * <code>string Creator = 5;</code>
     */
    public Builder setCreatorBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      creator_ = value;
      onChanged();
      return this;
    }

    private com.google.protobuf.ByteString hash_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>bytes Hash = 6;</code>
     */
    public com.google.protobuf.ByteString getHash() {
      return hash_;
    }
    /**
     * <code>bytes Hash = 6;</code>
     */
    public Builder setHash(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      hash_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>bytes Hash = 6;</code>
     */
    public Builder clearHash() {
      
      hash_ = getDefaultInstance().getHash();
      onChanged();
      return this;
    }

    private java.lang.Object hex_ = "";
    /**
     * <code>string Hex = 7;</code>
     */
    public java.lang.String getHex() {
      java.lang.Object ref = hex_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        hex_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string Hex = 7;</code>
     */
    public com.google.protobuf.ByteString
        getHexBytes() {
      java.lang.Object ref = hex_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        hex_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string Hex = 7;</code>
     */
    public Builder setHex(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      hex_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string Hex = 7;</code>
     */
    public Builder clearHex() {
      
      hex_ = getDefaultInstance().getHex();
      onChanged();
      return this;
    }
    /**
     * <code>string Hex = 7;</code>
     */
    public Builder setHexBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      hex_ = value;
      onChanged();
      return this;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }


    // @@protoc_insertion_point(builder_scope:poset.proto.Event)
  }

  // @@protoc_insertion_point(class_scope:poset.proto.Event)
  private static final poset.proto.Event DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new poset.proto.Event();
  }

  public static poset.proto.Event getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Event>
      PARSER = new com.google.protobuf.AbstractParser<Event>() {
    public Event parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new Event(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Event> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Event> getParserForType() {
    return PARSER;
  }

  public poset.proto.Event getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

