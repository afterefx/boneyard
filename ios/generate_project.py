#!/usr/bin/env python3
import os
import sys

def main():
    print("Generating Xcode project for PipTally...")
    
    # List of all source files
    swift_files = [
        "PipTally/PipTallyApp.swift",
        "PipTally/Utils/GameConstants.swift",
        "PipTally/Utils/ThemeColors.swift",
        "PipTally/Models/DominoModels.swift",
        "PipTally/Views/Components/PlayerAvatar.swift",
        "PipTally/Views/Components/DominoTile.swift",
        "PipTally/Views/Components/ScoreboardTable.swift",
        "PipTally/Views/Components/ScoreEntryRow.swift",
        "PipTally/Views/HomeView.swift",
        "PipTally/Views/PlayerListView.swift",
        "PipTally/Views/PlayerEditView.swift",
        "PipTally/Views/PlayerProfileView.swift",
        "PipTally/Views/GameSetupView.swift",
        "PipTally/Views/ActiveGameView.swift",
        "PipTally/Views/GameSummaryView.swift",
        "PipTally/Views/GameHistoryView.swift",
        "PipTally/Views/SettingsView.swift",
    ]
    
    # Ensure directories exist
    os.makedirs("PipTally.xcodeproj", exist_ok=True)
    
    # Generate UUIDs based on indices to ensure stable generation
    def get_uuid(index, prefix):
        return f"{prefix}{index:016X}"
        
    file_uuids = {}
    build_uuids = {}
    
    for i, path in enumerate(swift_files):
        file_uuids[path] = get_uuid(i, "AA")
        build_uuids[path] = get_uuid(i, "BB")
        
    # Generate sections
    pbx_build_files = []
    pbx_file_references = []
    pbx_sources = []
    
    for path in swift_files:
        name = os.path.basename(path)
        file_uuid = file_uuids[path]
        build_uuid = build_uuids[path]
        
        pbx_build_files.append(f"\t\t{build_uuid} /* {name} in Sources */ = {{isa = PBXBuildFile; fileRef = {file_uuid} /* {name} */; }};")
        pbx_file_references.append(f"\t\t{file_uuid} /* {name} */ = {{isa = PBXFileReference; lastKnownFileType = sourcecode.swift; path = {name}; sourceTree = \"<group>\"; }};")
        pbx_sources.append(f"\t\t\t\t{build_uuid} /* {name} in Sources */,")
        
    group_definitions = """
/* Begin PBXGroup section */
		CC0000000000000001 = {
			isa = PBXGroup;
			children = (
				FF0000000000000001 /* PipTally */,
				DD0000000000000003 /* Products */,
			);
			sourceTree = "<group>";
		};
		DD0000000000000003 /* Products */ = {
			isa = PBXGroup;
			children = (
				DD0000000000000002 /* PipTally.app */,
			);
			name = Products;
			sourceTree = "<group>";
		};
		FF0000000000000001 /* PipTally */ = {
			isa = PBXGroup;
			children = (
				AA0000000000000000 /* PipTallyApp.swift */,
				FF0000000000000005 /* Utils */,
				FF0000000000000002 /* Models */,
				FF0000000000000003 /* Views */,
			);
			path = PipTally;
			sourceTree = "<group>";
		};
		FF0000000000000002 /* Models */ = {
			isa = PBXGroup;
			children = (
				AA0000000000000003 /* DominoModels.swift */,
			);
			path = Models;
			sourceTree = "<group>";
		};
		FF0000000000000005 /* Utils */ = {
			isa = PBXGroup;
			children = (
				AA0000000000000001 /* GameConstants.swift */,
				AA0000000000000002 /* ThemeColors.swift */,
			);
			path = Utils;
			sourceTree = "<group>";
		};
		FF0000000000000003 /* Views */ = {
			isa = PBXGroup;
			children = (
				FF0000000000000004 /* Components */,
				AA0000000000000008 /* HomeView.swift */,
				AA0000000000000009 /* PlayerListView.swift */,
				AA000000000000000A /* PlayerEditView.swift */,
				AA000000000000000B /* PlayerProfileView.swift */,
				AA000000000000000C /* GameSetupView.swift */,
				AA000000000000000D /* ActiveGameView.swift */,
				AA000000000000000E /* GameSummaryView.swift */,
				AA000000000000000F /* GameHistoryView.swift */,
				AA0000000000000010 /* SettingsView.swift */,
			);
			path = Views;
			sourceTree = "<group>";
		};
		FF0000000000000004 /* Components */ = {
			isa = PBXGroup;
			children = (
				AA0000000000000004 /* PlayerAvatar.swift */,
				AA0000000000000005 /* DominoTile.swift */,
				AA0000000000000006 /* ScoreboardTable.swift */,
				AA0000000000000007 /* ScoreEntryRow.swift */,
			);
			path = Components;
			sourceTree = "<group>";
		};
/* End PBXGroup section */
"""

    project_template = """// !$*UTF8*$!
{
	archiveVersion = 1;
	classes = {
	};
	objectVersion = 56;
	objects = {

/* Begin PBXBuildFile section */
__PBX_BUILD_FILES__
/* End PBXBuildFile section */

/* Begin PBXFileReference section */
		DD0000000000000002 /* PipTally.app */ = {isa = PBXFileReference; explicitFileType = wrapper.application; includeInIndex = 0; path = PipTally.app; sourceTree = BUILT_PRODUCTS_DIR; };
__PBX_FILE_REFERENCES__
/* End PBXFileReference section */

/* Begin PBXFrameworksBuildPhase section */
		DD0000000000000005 /* Frameworks */ = {
			isa = PBXFrameworksBuildPhase;
			buildActionMask = 2147483647;
			files = (
			);
			runOnlyForDeploymentPostprocessing = 0;
		};
/* End PBXFrameworksBuildPhase section */

__GROUP_DEFINITIONS__

/* Begin PBXNativeTarget section */
		DD0000000000000001 /* PipTally */ = {
			isa = PBXNativeTarget;
			buildConfigurationList = EE0000000000000004 /* Build configuration list for PBXNativeTarget "PipTally" */;
			buildPhases = (
				DD0000000000000004 /* Sources */,
				DD0000000000000005 /* Frameworks */,
				DD0000000000000006 /* Resources */,
			);
			buildRules = (
			);
			name = PipTally;
			productName = PipTally;
			productReference = DD0000000000000002 /* PipTally.app */;
			productType = "com.apple.product-type.application";
		};
/* End PBXNativeTarget section */

/* Begin PBXProject section */
		CC0000000000000002 /* Project object */ = {
			isa = PBXProject;
			attributes = {
				BuildIndependentTargetsInParallel = 1;
				LastSwiftUpdateCheck = 1500;
				LastUpgradeCheck = 1500;
				TargetAttributes = {
					DD0000000000000001 = {
						CreatedOnToolsVersion = 15.0;
						LastSwiftMigration = 1500;
					};
				};
			};
			buildConfigurationList = EE0000000000000001 /* Build configuration list for PBXProject "PipTally" */;
			compatibilityVersion = "Xcode 14.0";
			developmentRegion = en;
			hasScannedForEncodings = 0;
			knownRegions = (
				en,
				Base,
			);
			mainGroup = CC0000000000000001;
			productRefGroup = DD0000000000000003 /* Products */;
			projectDirPath = "";
			projectRoot = "";
			targets = (
				DD0000000000000001 /* PipTally */,
			);
		};
/* End PBXProject section */

/* Begin PBXResourcesBuildPhase section */
		DD0000000000000006 /* Resources */ = {
			isa = PBXResourcesBuildPhase;
			buildActionMask = 2147483647;
			files = (
			);
			runOnlyForDeploymentPostprocessing = 0;
		};
/* End PBXResourcesBuildPhase section */

/* Begin PBXSourcesBuildPhase section */
		DD0000000000000004 /* Sources */ = {
			isa = PBXSourcesBuildPhase;
			buildActionMask = 2147483647;
			files = (
__PBX_SOURCES__
			);
			runOnlyForDeploymentPostprocessing = 0;
		};
/* End PBXSourcesBuildPhase section */

/* Begin XCBuildConfiguration section */
		EE0000000000000002 /* Debug */ = {
			isa = XCBuildConfiguration;
			buildSettings = {
				ALWAYS_SEARCH_USER_PATHS = NO;
				ASSETCATALOG_COMPILER_GENERATE_SWIFT_ASSET_SYMBOL_EXTENSIONS = YES;
				CLANG_ANALYZER_NONNULL = YES;
				CLANG_ANALYZER_NUMBER_OBJECT_CONVERSION = YES_AGGRESSIVE;
				CLANG_CXX_LANGUAGE_STANDARD = "gnu++20";
				CLANG_CXX_LIBRARY = "libc++";
				CLANG_ENABLE_MODULES = YES;
				CLANG_ENABLE_OBJC_ARC = YES;
				CLANG_ENABLE_OBJC_WEAK = YES;
				CLANG_WARN_BLOCK_CAPTURE_AUTORELEASING = YES;
				CLANG_WARN_BOOL_CONVERSION = YES;
				CLANG_WARN_COMMA = YES;
				CLANG_WARN_CONSTANT_CONVERSION = YES;
				CLANG_WARN_DEPRECATED_OBJC_IMPLEMENTATIONS = YES;
				CLANG_WARN_DIRECT_OBJC_PREPROCESSOR_DEFINITIONS = YES;
				CLANG_WARN_DOCUMENTATION_COMMENTS = YES;
				CLANG_WARN_EMPTY_BODY = YES;
				CLANG_WARN_ENUM_CONVERSION = YES;
				CLANG_WARN_INFINITE_RECURSION = YES;
				CLANG_WARN_INT_CONVERSION = YES;
				CLANG_WARN_NON_LITERAL_NULL_CONVERSION = YES;
				CLANG_WARN_OBJC_IMPLICIT_RETAIN_SELF = YES;
				CLANG_WARN_OBJC_LITERAL_CONVERSION = YES;
				CLANG_WARN_OBJC_ROOT_CLASS = YES_ERROR;
				CLANG_WARN_QUICKTIME_CONVERSION = YES;
				CLANG_WARN_RANGE_LOOP_ANALYSIS = YES;
				CLANG_WARN_STRICT_PROTOTYPES = YES;
				CLANG_WARN_SUSPICIOUS_MOVE = YES;
				CLANG_WARN_UNGUARDED_AVAILABILITY = YES_AGGRESSIVE;
				CLANG_WARN_UNREACHABLE_CODE = YES;
				CLANG_WARN__DUPLICATE_METHOD_MATCH = YES;
				COPY_PHASE_STRIP = NO;
				DEBUG_INFORMATION_FORMAT = dwarf;
				ENABLE_STRICT_OBJC_MSGSEND = YES;
				ENABLE_TESTABILITY = YES;
				ENABLE_USER_SCRIPT_SANDBOXING = NO;
				GCC_C_LANGUAGE_STANDARD = gnu17;
				GCC_DYNAMIC_NO_PIC = NO;
				GCC_NO_COMMON_BLOCKS = YES;
				GCC_OPTIMIZATION_LEVEL = 0;
				GCC_PREPROCESSOR_DEFINITIONS = (
					"DEBUG=1",
					"$(inherited)",
				);
				GCC_WARN_64_TO_32_BIT_CONVERSION = YES;
				GCC_WARN_ABOUT_RETURN_TYPE = GCC_WARN_ABOUT_RETURN_TYPE;
				GCC_WARN_UNDECLARED_SELECTOR = YES;
				GCC_WARN_UNINITIALIZED_ACTUAL = YES_AGGRESSIVE;
				GCC_WARN_UNUSED_FUNCTION = YES;
				GCC_WARN_UNUSED_VARIABLE = YES;
				IPHONEOS_DEPLOYMENT_TARGET = 17.0;
				MTL_ENABLE_DEBUG_INFO = INCLUDE_SOURCE;
				MTL_FAST_MATH = YES;
				ONLY_ACTIVE_ARCH = YES;
				SDKROOT = iphoneos;
				SWIFT_ACTIVE_COMPILATION_CONDITIONS = DEBUG;
				SWIFT_OPTIMIZATION_LEVEL = "-Onone";
			};
			name = Debug;
		};
		EE0000000000000003 /* Release */ = {
			isa = XCBuildConfiguration;
			buildSettings = {
				ALWAYS_SEARCH_USER_PATHS = NO;
				ASSETCATALOG_COMPILER_GENERATE_SWIFT_ASSET_SYMBOL_EXTENSIONS = YES;
				CLANG_ANALYZER_NONNULL = YES;
				CLANG_ANALYZER_NUMBER_OBJECT_CONVERSION = YES_AGGRESSIVE;
				CLANG_CXX_LANGUAGE_STANDARD = "gnu++20";
				CLANG_CXX_LIBRARY = "libc++";
				CLANG_ENABLE_MODULES = YES;
				CLANG_ENABLE_OBJC_ARC = YES;
				CLANG_ENABLE_OBJC_WEAK = YES;
				CLANG_WARN_BLOCK_CAPTURE_AUTORELEASING = YES;
				CLANG_WARN_BOOL_CONVERSION = YES;
				CLANG_WARN_COMMA = YES;
				CLANG_WARN_CONSTANT_CONVERSION = YES;
				CLANG_WARN_DEPRECATED_OBJC_IMPLEMENTATIONS = YES;
				CLANG_WARN_DIRECT_OBJC_PREPROCESSOR_DEFINITIONS = YES;
				CLANG_WARN_DOCUMENTATION_COMMENTS = YES;
				CLANG_WARN_EMPTY_BODY = YES;
				CLANG_WARN_ENUM_CONVERSION = YES;
				CLANG_WARN_INFINITE_RECURSION = YES;
				CLANG_WARN_INT_CONVERSION = YES;
				CLANG_WARN_NON_LITERAL_NULL_CONVERSION = YES;
				CLANG_WARN_OBJC_IMPLICIT_RETAIN_SELF = YES;
				CLANG_WARN_OBJC_LITERAL_CONVERSION = YES;
				CLANG_WARN_OBJC_ROOT_CLASS = YES_ERROR;
				CLANG_WARN_QUICKTIME_CONVERSION = YES;
				CLANG_WARN_RANGE_LOOP_ANALYSIS = YES;
				CLANG_WARN_STRICT_PROTOTYPES = YES;
				CLANG_WARN_SUSPICIOUS_MOVE = YES;
				CLANG_WARN_UNGUARDED_AVAILABILITY = YES_AGGRESSIVE;
				CLANG_WARN_UNREACHABLE_CODE = YES;
				CLANG_WARN__DUPLICATE_METHOD_MATCH = YES;
				COPY_PHASE_STRIP = YES;
				DEBUG_INFORMATION_FORMAT = "dwarf-with-dsym";
				ENABLE_STRICT_OBJC_MSGSEND = YES;
				ENABLE_USER_SCRIPT_SANDBOXING = NO;
				GCC_C_LANGUAGE_STANDARD = gnu17;
				GCC_NO_COMMON_BLOCKS = YES;
				GCC_WARN_64_TO_32_BIT_CONVERSION = YES;
				GCC_WARN_ABOUT_RETURN_TYPE = GCC_WARN_ABOUT_RETURN_TYPE;
				GCC_WARN_UNDECLARED_SELECTOR = YES;
				GCC_WARN_UNINITIALIZED_ACTUAL = YES_AGGRESSIVE;
				GCC_WARN_UNUSED_FUNCTION = YES;
				GCC_WARN_UNUSED_VARIABLE = YES;
				IPHONEOS_DEPLOYMENT_TARGET = 17.0;
				MTL_ENABLE_DEBUG_INFO = NO;
				MTL_FAST_MATH = YES;
				SDKROOT = iphoneos;
				SWIFT_COMPILATION_MODE = wholemodule;
				SWIFT_OPTIMIZATION_LEVEL = "-O";
			};
			name = Release;
		};
		EE0000000000000005 /* Debug */ = {
			isa = XCBuildConfiguration;
			buildSettings = {
				ASSETCATALOG_COMPILER_APPICON_NAME = AppIcon;
				ASSETCATALOG_COMPILER_GLOBAL_ACCENT_COLOR_NAME = AccentColor;
				CODE_SIGN_STYLE = Automatic;
				CURRENT_PROJECT_VERSION = 1;
				DEVELOPMENT_ASSET_PATHS = "";
				ENABLE_PREVIEWS = YES;
				GENERATE_INFOPLIST_FILE = YES;
				INFOPLIST_KEY_UIApplicationSceneManifest_Generation = YES;
				INFOPLIST_KEY_UILaunchScreen_Generation = YES;
				IPHONEOS_DEPLOYMENT_TARGET = 17.0;
				LD_RUNPATH_SEARCH_PATHS = (
					"$(inherited)",
					"@executable_path/Frameworks",
				);
				MARKETING_VERSION = 1.0;
				PRODUCT_BUNDLE_IDENTIFIER = app.piptally;
				PRODUCT_NAME = "$(TARGET_NAME)";
				SUPPORTED_PLATFORMS = "iphoneos iphonesimulator";
				SUPPORTS_MACCATALYST = NO;
				SWIFT_EMIT_LOC_STRINGS = YES;
				SWIFT_VERSION = 5.0;
				TARGETED_DEVICE_FAMILY = "1,2";
			};
			name = Debug;
		};
		EE0000000000000006 /* Release */ = {
			isa = XCBuildConfiguration;
			buildSettings = {
				ASSETCATALOG_COMPILER_APPICON_NAME = AppIcon;
				ASSETCATALOG_COMPILER_GLOBAL_ACCENT_COLOR_NAME = AccentColor;
				CODE_SIGN_STYLE = Automatic;
				CURRENT_PROJECT_VERSION = 1;
				DEVELOPMENT_ASSET_PATHS = "";
				ENABLE_PREVIEWS = YES;
				GENERATE_INFOPLIST_FILE = YES;
				INFOPLIST_KEY_UIApplicationSceneManifest_Generation = YES;
				INFOPLIST_KEY_UILaunchScreen_Generation = YES;
				IPHONEOS_DEPLOYMENT_TARGET = 17.0;
				LD_RUNPATH_SEARCH_PATHS = (
					"$(inherited)",
					"@executable_path/Frameworks",
				);
				MARKETING_VERSION = 1.0;
				PRODUCT_BUNDLE_IDENTIFIER = app.piptally;
				PRODUCT_NAME = "$(TARGET_NAME)";
				SUPPORTED_PLATFORMS = "iphoneos iphonesimulator";
				SUPPORTS_MACCATALYST = NO;
				SWIFT_EMIT_LOC_STRINGS = YES;
				SWIFT_VERSION = 5.0;
				TARGETED_DEVICE_FAMILY = "1,2";
			};
			name = Release;
		};
/* End XCBuildConfiguration section */

/* Begin XCConfigurationList section */
		EE0000000000000001 /* Build configuration list for PBXProject "PipTally" */ = {
			isa = XCConfigurationList;
			buildConfigurations = (
				EE0000000000000002 /* Debug */,
				EE0000000000000003 /* Release */,
			);
			defaultConfigurationIsVisible = 0;
			defaultConfigurationName = Release;
		};
		EE0000000000000004 /* Build configuration list for PBXNativeTarget "PipTally" */ = {
			isa = XCConfigurationList;
			buildConfigurations = (
				EE0000000000000005 /* Debug */,
				EE0000000000000006 /* Release */,
			);
			defaultConfigurationIsVisible = 0;
			defaultConfigurationName = Release;
		};
/* End XCConfigurationList section */
	};
	rootObject = CC0000000000000002 /* Project object */;
}
"""

    project_content = project_template
    project_content = project_content.replace("__PBX_BUILD_FILES__", "\n".join(pbx_build_files))
    project_content = project_content.replace("__PBX_FILE_REFERENCES__", "\n".join(pbx_file_references))
    project_content = project_content.replace("__GROUP_DEFINITIONS__", group_definitions.strip())
    project_content = project_content.replace("__PBX_SOURCES__", "\n".join(pbx_sources))

    with open("PipTally.xcodeproj/project.pbxproj", "w") as f:
        f.write(project_content)
        
    print("Xcode project generated successfully!")

if __name__ == "__main__":
    main()
