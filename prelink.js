#!/usr/bin/env node
const Path = require("path");
const fs = require("fs");

//Append the apply-plugin directive to the android/app/build.gradle
const pluginLine = "apply plugin: 'com.google.gms.google-services'";
const appGradlePath = Path.join(
  process.cwd(),
  "android",
  "app",
  "build.gradle"
);
if (!fs.existsSync(appGradlePath)) {
  console.log("Could not find app gradle: ", appGradlePath, "Aborting");
  process.exit(1);
}
const lines = fs.readFileSync(appGradlePath, { encoding: "UTF8" }).split("\n");
if (!lines.filter(line => line.indexOf(pluginLine) > -1).length) {
  lines.push(pluginLine);
  const out = lines.join("\n");
  fs.writeFileSync(appGradlePath, out);
}

//Insert the google services classpath to android/build.gradle buildscripts section
const projectGradlePath = Path.join(process.cwd(), "android", "build.gradle");
if (!fs.existsSync(projectGradlePath)) {
  console.log("could not find project gradle: ", projectGradlePath, "Aborting");
  process.exit(1);
}
const ptext = fs.readFileSync(projectGradlePath, { encoding: "UTF8" });
const newClassPathText = "classpath 'com.google.gms:google-services:4.2.0'";
if (ptext.indexOf(newClassPathText) === -1) {
  var plines = ptext.split("\n");

  const depIndex = plines.findIndex(line => {
    return line.indexOf("dependencies {") > -1;
  });
  if (!depIndex) {
    console.log(
      "Could not find dependencies in project gradle file",
      projectGradlePath
    );
    process.exit(1);
  }
  plines.splice(depIndex + 1, 0, [newClassPathText]);
  const newptext = plines.join("\n");
  fs.writeFileSync(projectGradlePath, newptext);
}
