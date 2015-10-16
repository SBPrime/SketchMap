<img src="http://i.imgur.com/4nAFu5K.png"></img>

<h1>About SketchMap</h1>

<p>SketchMap is a plugin designed to allow players to put images from the web onto a single or array of maps. These maps can be added to ItemFrames to complete the image and create awesome visual displays in vanilla minecraft.</p>
<p>SketchMap is currently built using Spigot 1.8 but should run  perfectly fine on all current 1.8 versions of Bukkit. </p>

<a href="https://github.com/slipswhitley/SketchMap" target="_blank">Original Github</a> </br>
<a href="https://github.com/trainphreak/SketchMap" target="_blank">Updated Github</a>
</br>
<h1>Guides:</h1>
<h3>  Creating a new SketchMap</h3>
<ol> 
  <li>Upload your desired image using a online tool like <a href="http://imgur.com>Imgur" target="_blank">Imgur</a> and copy the image URL.</li>
  <li>Now use the command "/sketchmap create <map-name> <URL>" Use Ctrl+V to paste an Image URL</li>
  <li>All done! SketchMap will split your image into 128x128 pixel chunks and give it to you as Maps! You can put these maps onto ItemFrames to show your completed Image!</li>
</ol>
<h3>  Some notes regarding large sketchmaps</h3>
Having a lot of map items (several thousand) in one area can cause serious client-side lag, even on high-end machines. Try to not make incredibly large sketchmaps; 100x100 would be really cool, but if players can't get close enough to see it, is it really worth it?</br>
You should also keep in mind that Minecraft has a hard limit of just over 65k maps, so if you create large sketchmaps, you will use up that limit very quickly! (a 50x50 sketchmap uses 2500 maps)
</br>
<h1>Commands, Permissions, and Configuration:</h1>
<h3>  Commands:</h3>
<ul>
  <li> /sketchmap create <MAP-ID> <URL> [XFRAMES]:[YFRAMES] -  Creates a new SketchMap using image @ URL</li>
  <li> /sketchmap get <MAP-ID> - Get a loaded map as Map Items</li>
  <li> /sketchmap place <MAP-ID> - Place an existing SketchMap with the upper-left corner on the block face you are looking at</li>
  <li> /sketchmap delete <MAP-ID> - Delete a loaded map</li>
  <li> /sketchmap list [PAGENUM] - List all current SketchMaps</li>
  <li> /sketchmap help - Display Plugin Information</li>
</ul>

<h3>  Permissions (Requires Vault - Otherwise OP is required)</h3>
<ul>
  <li> sketchmap.create</li>
  <li> sketchmap.get</li>
  <li> sketchmap.place</li>
  <li> sketchmap.delete</li>
  <li> sketchmap.list</li>
  <li> sketchmap.size.defaultexempt (exempts players from the size limit for creating/importing sketchmaps in the config file)</li>
  <li> sketchmap.size.# (put an actual number here to set a limit for a player or group; they must also have sketchmap.size.defaultexempt)</li>
</ul>
Note: if you are OP, you are able to run all the commands and you have no size limits.

<h3>  Configuration: </h3>
<ul>
  <li>default-max-dimension: (int) The maximum size of a sketchmap that can be created by a player without the defaultexempt permission</li>
</ul>
