# Changelog

## [Unreleased]
### Version notes

### Added

### Changed

### Removed

## [1.2.0]
### Version notes
Update compose-plugin 1.6.10->1.7.3, call update in setVisible of ComposeTopMostWindow,

### Added
internal lateinit var update: (ComposeWindow) -> Unit
fun getUndecoratedWindowDecorationClass(): KClass<*>
fun getWindowIsUndecorated(windowDecoration: WindowDecoration): Boolean
fun getResizerThicknessForWindowDecoration(windowDecoration: WindowDecoration): Dp


### Changed
class ComposeTopMostWindow internal constructor
internal object ComposeWindowHelper
fun TopMostWindow(
    decoration: WindowDecoration = WindowDecoration.SystemDefault,
    onCreate: OnCreateTopMostWindowEvent = null,
    beforeInitialization: InitializationEvent = DefaultBeforeInitializationEvent,
    afterInitialization: InitializationEvent = DefaultAfterInitializationEvent
)
### Removed

## [1.0.0] 2025-02-21
### Version notes
initial release

### Added

### Changed

### Removed