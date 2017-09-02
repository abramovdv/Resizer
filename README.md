# Resizer
[![](https://jitpack.io/v/hkk595/Resizer.svg)](https://jitpack.io/#hkk595/Resizer)

Inspired by zetbaitsu's [Compressor](https://github.com/zetbaitsu/Compressor), Resizer is a lightweight and easy-to-use Android library for image resizing. It allows you to create a smaller or bigger image from the original image while keeping the aspect ratio.

#### Include Resizer into your project
1. Add the JitPack repository to your top-level build.gradle at the end of repositories
```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
2. Add the dependency in your module-level build.gradle
```groovy
dependencies {
    compile 'com.github.hkk595:Resizer:v1.0'
}
```

#### Pass in the original image file and get the resized image as a new file
```java
File resizedImage = new Resizer(this)
        .setTargetLength(1080)
        .setQuality(80)
        .setOutputFormat("JPEG")
        .setDestinationDirPath(storagePath)
        .setSourceImage(originalImage)
        .getResizedFile();
```
#### Pass in the original image file and get the resized image as a new bitmap
```java
Bitmap resizedImage = new Resizer(this)
        .setTargetLength(1080)
        .setSourceImage(originalImage)
        .getResizedBitmap();
```
Note: You only need to specify the target length (in pixel) of the longer side of the image. Resizer will calculate the rest automatically.

#### Using RxJava to get the resized image asynchronously
```java
final File[] resizedImage = new File[1];
new Resizer(this)
        .setTargetLength(1080)
        .setQuality(80)
        .setOutputFormat("JPEG")
        .setDestinationDirPath(storagePath)
        .setSourceImage(originalImage)
        .getResizedFileAsFlowable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<File>() {
            @Override
            public void accept(File file) {
                resizedImage[0] = file;
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
```
```java
final Bitmap[] resizedImage = new Bitmap[1];
new Resizer(this)
        .setTargetLength(1080)
        .setSourceImage(originalImage)
        .getResizedBitmapAsFlowable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<Bitmap>() {
            @Override
            public void accept(Bitmap bitmap) {
                resizedImage[0] = bitmap;
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
```
Note: You don't need to declare the new image as final nor array if it is an instance variable of the class, instead of a local variable in a function.

#### Library specification
    Default settings:
    targetLength: 1080
    quality: 80
    outputFormat: JPEG
    destinationDirPath: the external files directory of your app
     
    Supported input formats:
    BMP
    GIF
    JPEG
    PNG
    WEBP
     
    Supported output formats:
    JPEG
    PNG
    WEBP
     
    Supported quality range:
    0~100
    The higher number the better in image quality and larger in file size
    PNG, which is a lossless format, will ignore the quality setting

## License
    MIT License
     
    Copyright (c) 2017 K.K. Ho
     
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
     
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
     
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.