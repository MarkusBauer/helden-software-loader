TODO TODO TODO TODO TODO
========================
Custom Entry Plugin für die Helden-Software
===========================================

Dieses Plugin ist in der Lage, eigene Zauber, Talente und Sprachen zur DSA 4.1 Helden-Software
von [www.helden-software.de](http://www.helden-software.de) hinzuzufügen.
Neue Zauber sind wahlweise per Editor oder zur regulären Aktivierung verfügbar,
neue Talente werden über das "Erweiterungen"-Menü zu einzelnen Helden hinzugefügt.

Nach der Installation können eigene Zauber in eine Konfigurationsdatei eingetragen
werden, und stehen dann in allen Helden zur Verfügung. Diese Konfigurationsdatei
könnt ihr auch an andere Spieler versenden, denen eure Zauber dann ebenfalls 
zur Verfügung stehen. 

Eigene Talente benötigen keine Konfiguration.

 
 
Warnung
-------
*Bitte lest diesen Abschnitt genau, wenn ihr eure Helden nicht verlieren wollt!*

Die Helden-Software weigert sich Helden zu laden, die nicht bekannte Zauber eingetragen haben. 
Daraus folgen einige Punkte, die man beachten sollte. 

**Helden mit eigenen Zaubern können nur mit Plugin geladen werden.**
Wenn ihr die Helden-Software ohne Plugin startet, fehlen entsprechende Helden. 
Wenn ihr das Plugin nicht mehr nutzen wollt, müsst ihr alle eigenen Zauber
vorher per Editor aus euren Helden entfernen. Für Talente gilt dies nicht.
 
**Helden können nur geladen werden, wenn alle eigenen Zauber dem Plugin bekannt sind.**
Bevor ihr also einen Zauber aus der Konfiguration löscht, muss der Zauber aus allen
Helden entfernt werden. Wenn ihr euren Helden exportiert und eurem Spielleiter schickt,
dann muss dieser das Plugin installiert haben, und alle eigenen Zauber konfiguriert haben.
Talente werden im Held gespeichert, und funktionieren trotzdem.

**Wenn die Helden-Software meldet, dass manche eurer Helden nicht geladen werden konnten - Software schließen und nicht speichern!**
Sonst sind die betreffenden Helden weg. Endgültig. Es gibt drei mögliche Gründe, 
warum dieser Fehler auftreten kann: 
 - Plugin wurde nicht richtig installiert oder geladen. Lest die Installations-Anleitung nochmal. 
 - Ein Zauber wurde aus der Konfiguration entfernt (oder umbenannt). Ein Spruch den Helden 
   aktiviert haben darf nicht entfernt oder umbenannt werden.
 - Die Helden-Software hat sich gerade aktualisiert und neugestartet. Ein weiterer Neustart 
   (natürlich ohne zu speichern) löst das Problem. 

**Talente mit gleichem Namen sollten gleich sein.** Sonst weigert sich der Editor später, das Talent zu bearbeiten.

**Macht Backups!**
Sollte eigentlich selbstverständlich sein, ist es aber leider nicht. 
Eure Helden liegen üblicherweise in eurem Benutzerverzeichnis (`C:\Benutzer\XXX\helden\helden.zip.hld` bzw. `~/helden/helden.zip.hld` unter Linux). 
Regelmäßiges Sichern dieser Datei kann euch viel Arbeit und viele Nerven sparen.
Außerdem ist es sinnvoll, die Konfiguration des Plugins gleich mitzusichern. 

**Keine Garantie.**
Dieses Plugin verwendet Schnittstellen, die nicht öffentlich dokumentiert sind, 
in einem Programm, dessen Quelltext dem Autor nicht vorliegt. Daher können 
ungewollte Nebenwirkungen nicht ganz ausgeschlossen werden (auch wenn bisher keine bekannt sind).
Aber da ihr ja alle Backups habt, sollte euch das nicht betreffen...



Installation
------------
Dieses Plugin kann *nicht* wie herkömmliche Helden-Software-Plugins installiert 
werden, da die offizielle Plugin-Schnittstelle nicht für solche Eingriffe gemacht ist.

Stattdessen muss dieses Plugin (die `CustomEntryLoader.jar`) die Datei sein, 
über die Ihr die Helden-Software startet. 

- Zuerst sucht Ihr den Ordner, in den die Helden-Software installiert ist. 
  Das ist der Ordner, in dem eine Datei namens `helden.jar` (oder `helden5.jar`) liegt.
- Kopiert die `CustomEntryLoader.jar` in diesen Ordner
- Legt eine neue Verknüpfung zur `CustomEntryLoader.jar` an, und
  benutzt ausschließlich diese.

  Alternativ: Ändert eure Verknüpfungen ab (bspw. die im Startmenü, Rechtsklick -> Eigenschaften).
  Ersetzt jedes Auftreten von `helden.jar` durch `CustomEntryLoader.jar`.


Solltet ihr die Helden-Software versehentlich über eine alte Verknüpfung starten
(ohne dass das Plugin geladen wird), werdet ihr mit einer großen Warnung begrüßt, 
dass manche eurer Helden nicht geladen werden können, und daher verloren gehen, 
sobald ihr erneut speichert. In diesem Fall: Keine Panik, schließen **ohne** zu
Speichern, und den Generator mit Plugin starten. 



Konfiguration
-------------
Neue Zauber werden nicht in der Helden-Software selber erstellt, sondern in eine
Konfigurationsdatei (`erweiterungen.json`) geschrieben. 
Diese Datei kommt ins gleiche Verzeichnis wie eure `helden.zip.hld` - also 
`C:\Benutzer\XXX\helden\erweiterungen.json` unter Windows bzw. `~/helden/erweiterungen.json` unter Linux.

Neue Talente benötigen keine Konfiguration, und können im "Erweiterungen"-Menü erstellt werden.

--- JSON-Dateien ---
Json-Dateien können mit einem beliebigen Texteditor angelegt oder bearbeitet werden
(bspw. dem "Editor" von Windows). Die Datei sollte mit der Codierung "UTF-8" 
gespeichert werden, sonst sind Umlaute später kaputt. Ein ordentlicher Editor 
wie bspw. "Sublime Text" oder "Notepad++" kann das Leben einfacher machen, ist 
aber nicht zwingend notwendig.

--- Grundstruktur ---
Eine leere `erweiterungen.json` ohne neue Zauber sieht so aus: 
{
    "zauber": [
        // Hier kommen eure neuen Zauber hin 
    ]
}
Die `//` sind Kommentarzeichen - alles was dahinter steht gehört nicht zum 
 eigentlichen Inhalt der Datei (und wird nicht mit eingelesen). 
 
Ein einfacher Zauber sieht so aus: 
{
    "zauber": [
        {
            "name": "Bannbaladin (erhöhte Reichweite)",
            "kategorie": "B",
            "merkmale": ["Einfluss"],
            "probe": "IN/CH/CH",
            "settings": ["Aventurien"]
        }
    ]
}

--- Zauber konfigurieren ---
Der folgende Zauber verwendet alle möglichen Optionen:
{
    "zauber": [
        // === Inarcanitas - ein neuer Zauber aus unserer Runde ===
        {
            "name": "Inarcanitas", // Zaubername, darf noch nicht existieren
            "kategorie": "F", // A-H
            "merkmale": ["Anti", "Kraft", "Metamagie"], // Kurzfassung und Langfassung möglich
            "probe": "MU/KL/KL", // Probe "XX/YY/ZZ", auch "**" möglich
            "mod": "+Mod", // (optional) "+MR", "+Mod", ...
            "quelle": "LCD:123", // (optional) Format muss "XXX:<seitenzahl>" sein

            // Settings (optional)
            // In welchem Regel-System kommt der Spruch vor?
            // Die Auswahl eines / mehrerer Settings erlaubt es, den Spruch im Editor zu aktivieren
            // Mögliche Werte: "Alle", "DSA4.1"/"Aventurien", "Myranor", "Tharun", ...
            "settings": ["Aventurien"],

            // Verbreitungen (optional)
            // In welcher Repräsentation existiert der Spruch, wie häufig (0-7)?
            // Setting + Verbreitung erlaubt es, den Spruch regulär zu aktivieren
            "verbreitung": {
                "Mag": 7,
                "Elf": 4,
                "Hex(Mag)": 2  // Hexen können den Spruch in gildenmagischer Repräsentation lernen
            },

            // Spezialisierungen (optional)
            // Mögliche Zauberspezialisierungen (spontane Modifikationen, Varianten, ...)
            "spezialisierungen": [
                "Reichweite",
                "Zauberdauer",
                "Variante X"
            ]
        }
    ]
}
Zur Sichtbarkeit eines Zaubers gilt:
- Ist ein Setting gesetzt, kann der Zauber per Editor hinzugefügt werden
- Ist ein Setting und eine Verbreitung gesetzt, kann der Zauber (in diesen 
  Repräsentationen) aktiviert werden
- Wenn kein Setting gesetzt ist, kann der Zauber nicht hinzugefügt werden 
  (aber weiter gesteigert werden, wenn er schon aktiviert ist).


--- Weitere Beispiele ---
Diesem Archiv liegt eine exemplarische "examples.json" bei.


--- CSV ---
Da viele Tabellenkalkulationen den Export nach CSV ermöglichen und dies für viele Anwender dies einfacher zu handhaben ist,
besteht die Option neue Zauber auch über eine "erweiterungen.csv"-Datei zu importieren.
Die Konfiguration ist wie bei json zu handhaben:
Die "erweiterungen.csv" Datei, kommt ins gleiche Verzeichnis wie eure "helden.zip.hld", als "C:\Benutzer\XXX\helden\erweiterungen.csv" unter Windows
bzw. "~/helden/erweiterungen.csv" unter Linux.
Die folgende Tabelle veranschaulicht das Format:

Name,Kategorie,Merkmale,Probe,Mods/MR,Verbreitung,Settings
Mein Zauber,D,Antimagie,MU/KL/CH,,Mag3,Aventurien




Externe Komponenten
-------------------
Dieses Plugin basiert auf verschiedenen anderen Projekten:

- json-simple by *Yidong Fang*, Apache License 2.0(http://www.apache.org/licenses/LICENSE-2.0)
- OpenPojo(https://github.com/oshoukry/openpojo), Apache License 2.0(http://www.apache.org/licenses/LICENSE-2.0)
- IntelliJ Forms_rt by Jetbrains(http://www.jetbrains.com/), Apache License 2.0(http://www.apache.org/licenses/LICENSE-2.0)
- ASM Commons(http://asm.ow2.org/), Apache License 2.0(http://www.apache.org/licenses/LICENSE-2.0)



Kontakt
-------
 - "Rothen" bei [dsaforum.de](http://dsaforum.de/memberlist.php?mode=viewprofile&u=12050)
 - Per Mail: markus-7y5wrhdz (AT) mk-bauer.de
 
