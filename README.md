# Content Mask

Content mask is AEM plugin that allows for the "masking" of an arbitrary path in the resource tree with the content in a second path. Properties from the first path, known as the data path, is masked by any matching properties contained in the second path, known as the storage path.  Any properties from the data path without a corresponding storage path property will be visible. The concept is similar to Sling Resource Merger, with a few notable exceptions:

1. Resource Merger is bound to the resource resolver search path.  Content Mask uses arbitrary paths (which can be changed at runtime).
2. Resource Merger does not provide write access.  Content Mask resources can be written to, with any updates being pushed into the storage path.  The end result being that the data path remains unchanged, with modifications only visible in the masked view.
3. Resource Merger provides a single merged path (found at "/mnt/overlay").  Content Mask can provide any number of merged paths.  Each mask is defined by a type and a name, and can be resolved at "/mask:root/*$type*/*$name*"

Masks can be defined using JCR mixin types mask:Config and mask:NamedConfig. Named content masks use a user-defined name, where non-named masks use the JCR UUID field that the types add to the node. Both types require specification of the data path to create a mask for, and the storage path is created as a child node, with a relative path of "mask:storage". Any resource with these mixins can be adapted to a MaskConfig object, which provides access to the masked path where the merged data can be found.  Non-JCR based implementations (OSGi, etc) are also possible by creating a custom MaskConfigProvider implementation.

The OOTB node types for defining masks can be embedded in page/component templates, with data paths and/or names set via component dialogs. If desired, the templates can also insert a LiveSyncConfig node below the storage root.  This can be used to simulate MSM dialog functionality, with the explicit locking/unlocking of "inherited" fields (for dialogs that support it).

Possible use cases:

1. "Annotate" readonly data from a custom sling resource provider as if it were JCR content.  AEM components can be used to override/augment fields via component dialogs.
2. Share AEM component/page content, allowing content authored in one part of the JCR to be reused elsewhere. The shared content can be overridden via component dialogs as necessary.

AEM 6+ and CRX3 are required.