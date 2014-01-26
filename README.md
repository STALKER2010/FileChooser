FileChooser
===========

My own implementation of file chooser for Android.

Contains file/directory selection with ability to rename and delete files. Customizable and quick.

Just extend your Activity from FMActivity and call choose() method from onCreate(Bundle mBundle), and that is it!
To change settings of file picker, change mOpts.* fields in your Activity BEFORE calling choose().