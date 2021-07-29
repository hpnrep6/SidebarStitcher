# SidebarStitcher

Stitches images while also adding a bar on the right, with the option to make the bar fade into the main image.

## UI
![ex](https://user-images.githubusercontent.com/57055412/127446290-e7ef6be1-f1ac-4f72-90ba-f812a1e41d2f.png)

### Terms

- Inner sidebar width: Fade out of the main image into the sidebar. Colour is extrapolated from edge of main image.
- Outer sidebar width: Fade into the main image from the sidebar. Colour is added on top of main image.

### Modes

- No fade: Leave sidebar file and sidebar fade widths as is
- Fade: Select sidebar file and set sidebar fade widths as desired

### Text fields

Press `Enter` to enter your input into the application.

## Saving settings

A `WatermarkStitcher.config` file is automatically created and updated each time the stitcher is run successfully. This config file saves the selected files and settings used when `Stitch!` was pressed so that it can be used again in the future.

Clicking `Clear saved settings` will delete this file.

## Example

![stitched-sb_1627542655903](https://user-images.githubusercontent.com/57055412/127447886-1ea4f835-306a-4633-bba9-b3495efaa4b1.png)
