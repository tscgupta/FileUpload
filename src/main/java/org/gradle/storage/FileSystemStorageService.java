package org.gradle.storage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {
	
	private final Path rootLocation;
	
	@Autowired
	public FileSystemStorageService(StorageProperties properties){
		this.rootLocation = Paths.get(properties.getLocation());
	}
	public void init() {
		try{
			Files.createDirectory(rootLocation);
		} catch (IOException e){
			throw new StorageException("Could not initialize storage", e);
		}

	}
	

	public void store(MultipartFile file) {
		// TODO Auto-generated method stub
		try{
			if(file.isEmpty()){
				throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
			}
			Files.copy(file.getInputStream(), this.rootLocation.resolve(file.getOriginalFilename()));
		} catch (IOException e){
			throw new StorageException("Failed to store file " + file.getOriginalFilename(),e);
		}
	}

	public Stream<Path> loadAll() {
		// TODO Auto-generated method stub
		try{
			return Files.walk(this.rootLocation, 1)
					.filter(path -> !path.equals(this.rootLocation))
					.map(path -> this.rootLocation.relativize(path));
		} catch (IOException e){
			throw new StorageException("Failed to read stored files",e);
		}
		
	}

	public Path load(String filename) {
		
		return rootLocation.resolve(filename);
	}

	public Resource loadAsResource(String filename) {
		try {
			Path file = load(filename);
			Resource resource = new UrlResource(file.toUri());
			if(resource.exists() || resource.isReadable()){
				return resource;
			} else {
				throw new StorageFileNotFoundException("Could not read file: " + filename);
			}
		} catch (MalformedURLException e){
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());

	}

}
