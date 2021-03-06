package apps.oreonworlds.assets.plants;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import apps.oreonworlds.shaders.plants.GrassShader;
import engine.buffers.MeshVAO;
import engine.buffers.UBO;
import engine.configs.AlphaTestCullFaceDisable;
import engine.math.Vec3f;
import engine.scenegraph.GameObject;
import engine.scenegraph.components.RenderInfo;
import engine.scenegraph.components.Renderer;
import engine.scenegraph.components.TransformsInstanced;
import engine.utils.BufferAllocation;
import modules.instancing.InstancingCluster;
import modules.modelLoader.obj.Model;
import modules.modelLoader.obj.OBJLoader;
import modules.terrain.Terrain;

public class Grass01Instanced extends InstancingCluster{

	private List<TransformsInstanced> transforms = new ArrayList<TransformsInstanced>();
	
	private UBO modelMatricesBuffer;
	private UBO worldMatricesBuffer;

	private Vec3f center;

	public Grass01Instanced(int instances, Vec3f pos, int modelMatBinding, int worldMatBinding){
		
		center = pos;
		int buffersize = Float.BYTES * 16 * instances;
		setModelMatBinding(modelMatBinding);
		setWorldMatBinding(worldMatBinding);
		
		Model[] models = new OBJLoader().load("./res/oreonworlds/assets/plants/Grass_01","grassmodel.obj","grassmodel.mtl");
		
		for (int i=0; i<instances; i++){
			Vec3f translation = new Vec3f((float)(Math.random()*100)-50 + center.getX(), 0, (float)(Math.random()*100)-50 + center.getZ());
			float terrainHeight = Terrain.getInstance().getTerrainHeight(translation.getX(),translation.getZ());
			terrainHeight -= 1;
			translation.setY(terrainHeight);
			float s = (float)(Math.random()*2 + 6);
			Vec3f scaling = new Vec3f(s,s,s);
			Vec3f rotation = new Vec3f(0,(float) Math.random()*360f,0);
			
			TransformsInstanced transform = new TransformsInstanced();
			transform.setTranslation(translation);
			transform.setScaling(scaling);
			transform.setRotation(rotation);
			transform.setLocalRotation(rotation);
			transform.initMatrices();
			transforms.add(transform);
			getHighPolyIndices().add(i);
		}
		
		modelMatricesBuffer = new UBO();
		modelMatricesBuffer.setBinding_point_index(modelMatBinding);
		modelMatricesBuffer.bindBufferBase();
		modelMatricesBuffer.allocate(buffersize);
		
		worldMatricesBuffer = new UBO();
		worldMatricesBuffer.setBinding_point_index(worldMatBinding);
		worldMatricesBuffer.bindBufferBase();
		worldMatricesBuffer.allocate(buffersize);	
		
		/**
		 * init matrices UBO's
		 */
		int size = Float.BYTES * 16 * instances;
		
		FloatBuffer worldMatricesFloatBuffer = BufferAllocation.createFloatBuffer(size);
		FloatBuffer modelMatricesFloatBuffer = BufferAllocation.createFloatBuffer(size);
		
		for(TransformsInstanced matrix : transforms){
			worldMatricesFloatBuffer.put(BufferAllocation.createFlippedBuffer(matrix.getWorldMatrix()));
			modelMatricesFloatBuffer.put(BufferAllocation.createFlippedBuffer(matrix.getModelMatrix()));
		}
		
		worldMatricesBuffer.updateData(worldMatricesFloatBuffer, size);
		modelMatricesBuffer.updateData(modelMatricesFloatBuffer, size);
		
		for (Model model : models){
			
			GameObject object = new GameObject();
			MeshVAO meshBuffer = new MeshVAO();
			model.getMesh().setTangentSpace(false);
			model.getMesh().setInstanced(true);
			model.getMesh().setInstances(instances);
			
			
			meshBuffer.addData(model.getMesh());

			object.setRenderInfo(new RenderInfo(new AlphaTestCullFaceDisable(0.6f), GrassShader.getInstance()));
				
			Renderer renderer = new Renderer(object.getRenderInfo().getShader(), meshBuffer);

			object.addComponent("Material", model.getMaterial());
			object.addComponent("Renderer", renderer);
			addChild(object);
		}
	}
}
