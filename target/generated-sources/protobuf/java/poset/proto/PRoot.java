// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: poset/root.proto

package poset.proto;

public final class PRoot {
  private PRoot() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_poset_proto_RootEvent_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_poset_proto_RootEvent_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_poset_proto_Root_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_poset_proto_Root_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_poset_proto_Root_OthersEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_poset_proto_Root_OthersEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\020poset/root.proto\022\013poset.proto\"d\n\tRootE" +
      "vent\022\014\n\004Hash\030\001 \001(\t\022\021\n\tCreatorID\030\002 \001(\003\022\r\n" +
      "\005Index\030\003 \001(\003\022\030\n\020LamportTimestamp\030\004 \001(\003\022\r" +
      "\n\005Round\030\005 \001(\003\"\273\001\n\004Root\022\021\n\tNextRound\030\001 \001(" +
      "\003\022*\n\nSelfParent\030\002 \001(\0132\026.poset.proto.Root" +
      "Event\022-\n\006Others\030\003 \003(\0132\035.poset.proto.Root" +
      ".OthersEntry\032E\n\013OthersEntry\022\013\n\003key\030\001 \001(\t" +
      "\022%\n\005value\030\002 \001(\0132\026.poset.proto.RootEvent:" +
      "\0028\001B\tB\005PRootP\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_poset_proto_RootEvent_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_poset_proto_RootEvent_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_poset_proto_RootEvent_descriptor,
        new java.lang.String[] { "Hash", "CreatorID", "Index", "LamportTimestamp", "Round", });
    internal_static_poset_proto_Root_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_poset_proto_Root_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_poset_proto_Root_descriptor,
        new java.lang.String[] { "NextRound", "SelfParent", "Others", });
    internal_static_poset_proto_Root_OthersEntry_descriptor =
      internal_static_poset_proto_Root_descriptor.getNestedTypes().get(0);
    internal_static_poset_proto_Root_OthersEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_poset_proto_Root_OthersEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}