function insertMedia()
    local media = mediaFile()
	
	if media then
	    reaper.InsertMedia(media, 0)
	else
	    log("\nMedia file not found!\n")
	end
end

function render()
    reaper.Main_OnCommand(42230, 1)
    projectRendered = true
  
    waitForSave()
end

function waitForSave()
    isDirty = reaper.IsProjectDirty(project)
    
    if isDirty == 1 then
	    projectRendered = false
        reaper.defer(waitForSave)
    else
        if not projectRendered then
            render()
        end
    end  
end

function log(msg)
    reaper.ShowConsoleMsg(msg)
end

function mediaFile()
    local appdata = windowsAppData()
	
	if not appdata then
	    appdata = macAppData()
	end
		
	if not appdata then
	    appdata = linuxAppData()
    end		
	
	if appdata then
	    local logFile = appdata .. "/ReaperOraturePlugin/media.log"
		local media = io.open(logFile, "r")
		
		if media then
		    io.input(media)
			local path = io.read()
			io.close()
			
			return path
        else
		    return nil
		end
	else
	    return nil
    end		
end

function windowsAppData()
    return os.getenv("APPDATA")
end

function macAppData()
    local home = os.getenv("HOME")
	if home then
	    local path = home .. "/Library/Application Support"
		if exists(path .. "/") then
		    return path
		else
		    return nil
		end
	else
	    return nil
	end
end

function linuxAppData()
    local home = os.getenv("HOME")
	if home then
	    local path = home .. "/.config"
		if exists(path .. "/") then
		    return path
		else
		    return nil
		end
	else
	    return nil
	end
end

function exists(file)
   local ok, err, code = os.rename(file, file)
   if not ok then
      if code == 13 then
         -- Permission denied, but it exists
         return true
      end
   end
   return ok, err
end

projectRendered = true
project = reaper.EnumProjects(-1)

insertMedia()
waitForSave()
