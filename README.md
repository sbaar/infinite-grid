infinite-grid
=============

Android Infinite Image Grid

This project creates an infinitely tileable image grid. It can be swiped horizontally, vertically or diagonally, and uses a deceleration flinger.

The database records and images reside on Parse.com.  The application uses headless fragments that create AsyncTasks (to retain across activity restarts) to download b records and images.

Aside from the grid, the app demonstrates AsyncTasks using fragments, a simple REST GET call and simple JSON parsing.
  
Images are cached to disk using DiskLruCache found here: https://github.com/JakeWharton/DiskLruCache
