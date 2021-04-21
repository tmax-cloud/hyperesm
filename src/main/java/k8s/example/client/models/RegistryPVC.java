package k8s.example.client.models;

import java.util.ArrayList;
import java.util.List;

public class RegistryPVC {
	public class ExistPvc {
		private String pvcName = null;

		public String getPvcName() {
			return pvcName;
		}

		public void setPvcName(String pvcName) {
			this.pvcName = pvcName;
		}
	}
	
	public class CreatePvc {
		private List<String> accessModes = null;
		private String storageSize = null;
		private String storageClassName = null;
		private String volumeMode = null;
		private Boolean deleteWithPvc = null;
		
		public List<String> getAccessModes() {
			return accessModes;
		}
		public void setAccessModes(List<String> accessModes) {
			this.accessModes = accessModes;
		}
		public String getStorageSize() {
			return storageSize;
		}
		public void setStorageSize(String storageSize) {
			this.storageSize = storageSize;
		}
		public String getStorageClassName() {
			return storageClassName;
		}
		public void setStorageClassName(String storageClassName) {
			this.storageClassName = storageClassName;
		}
		public String getVolumeMode() {
			return volumeMode;
		}
		public void setVolumeMode(String volumeMode) {
			this.volumeMode = volumeMode;
		}
		public Boolean getDeleteWithPvc() {
			return deleteWithPvc;
		}
		public void setDeleteWithPvc(Boolean deleteWithPvc) {
			this.deleteWithPvc = deleteWithPvc;
		}
		public CreatePvc addAccessModesItem(String accessMode) {
			if (this.accessModes == null) {
				this.accessModes = new ArrayList<String>();
			}
			this.accessModes.add(accessMode);
			return this;
		}
		
	}
	
//	public static final String STORAGE_CLASS_DEFAULT = "csi-cephfs-sc";
	public static final String ACCESS_MODE_DEFAULT = "ReadWriteMany";
	private String mountPath = null;
	private ExistPvc exist = null;
	private CreatePvc create = null;
	
	public String getMountPath() {
		return mountPath;
	}
	public void setMountPath(String mountPath) {
		this.mountPath = mountPath;
	}
	public ExistPvc getExist() {
		return exist;
	}
	public void setExist(ExistPvc exist) {
		this.exist = exist;
	}
	public CreatePvc getCreate() {
		return create;
	}
	public void setCreate(CreatePvc create) {
		this.create = create;
	}
}
