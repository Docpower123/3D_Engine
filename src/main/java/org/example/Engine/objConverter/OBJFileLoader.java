package org.example.Engine.objConverter;

import org.example.Engine.Loader;
import org.example.Engine.models.RawModel;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class OBJFileLoader {

    private static final String RES_LOC = "res/";

    /**
     * Loads an OBJ file and converts it to a RawModel.
     *
     * @param objFileName The name of the OBJ file to load.
     * @param loader The loader used to load the model data.
     * @return The loaded RawModel.
     */
    public static RawModel loadOBJ(String objFileName, Loader loader) {
        FileReader isr = null;
        String fileName = RES_LOC + objFileName + ".obj";
        File objFile = new File(fileName);
        System.out.println("OBJFileLoader: loading file: " + fileName);
        try {
            isr = new FileReader(objFile);
        } catch (FileNotFoundException e) {
            System.err.println("OBJFileLoader: File not found: " + fileName);
        }
        BufferedReader reader = new BufferedReader(isr);
        String line;
        List<Vertex> vertices = new ArrayList<>();
        List<Vector2f> textures = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int vCount = 0;
        int vtCount = 0;
        int vnCount = 0;
        int fCount = 0;
        try {
            while (true) {
                line = reader.readLine();
                if (line.startsWith("v ")) {
                    String[] currentLine = line.split(" ");
                    Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3]));
                    Vertex newVertex = new Vertex(vertices.size(), vertex);
                    vertices.add(newVertex);
                    vCount++;
                } else if (line.startsWith("vt ")) {
                    String[] currentLine = line.split(" ");
                    Vector2f texture = new Vector2f(Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]));
                    textures.add(texture);
                    vtCount++;
                } else if (line.startsWith("vn ")) {
                    String[] currentLine = line.split(" ");
                    Vector3f normal = new Vector3f(Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3]));
                    normals.add(normal);
                    vnCount++;
                } else if (line.startsWith("f ")) {
                    break;
                }
            }
            while (line != null && line.startsWith("f ")) {
                String[] currentLine = line.split(" ");
                String[] vertex1 = currentLine[1].split("/");
                String[] vertex2 = currentLine[2].split("/");
                String[] vertex3 = currentLine[3].split("/");
                Vertex v0 = processVertex(vertex1, vertices, indices);
                Vertex v1 = processVertex(vertex2, vertices, indices);
                Vertex v2 = processVertex(vertex3, vertices, indices);
                calculateTangents(v0, v1, v2, textures);
                line = reader.readLine();
                fCount++;
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("OBJFileLoader: Error reading the file: " + fileName);
        }

        System.out.println("OBJFileLoader:"
                + " vertices: " + vCount
                + " textureCoords: " + vtCount
                + " normals: " + vnCount
                + " faces: " + fCount);

        removeUnusedVertices(vertices);
        float[] verticesArray = new float[vertices.size() * 3];
        float[] texturesArray = new float[vertices.size() * 2];
        float[] normalsArray = new float[vertices.size() * 3];
        float[] tangentsArray = new float[vertices.size() * 3];
        convertDataToArrays(vertices, textures, normals, verticesArray,
                texturesArray, normalsArray, tangentsArray);
        int[] indicesArray = convertIndicesListToArray(indices);
        return loader.loadToVAO(verticesArray, texturesArray, normalsArray, indicesArray);
    }

    /**
     * Calculates the tangents for the given vertices.
     *
     * @param v0 The first vertex.
     * @param v1 The second vertex.
     * @param v2 The third vertex.
     * @param textures The list of texture coordinates.
     */
    private static void calculateTangents(Vertex v0, Vertex v1, Vertex v2,
                                          List<Vector2f> textures) {
        Vector3f deltaPos1 = new Vector3f(v1.getPosition());
        deltaPos1.sub(v0.getPosition());
        Vector3f deltaPos2 = new Vector3f(v2.getPosition());
        deltaPos2.sub(v0.getPosition());

        Vector2f uv0 = textures.get(v0.getTextureIndex());
        Vector2f uv1 = textures.get(v1.getTextureIndex());
        Vector2f uv2 = textures.get(v2.getTextureIndex());

        Vector2f deltaUv1 = new Vector2f(uv1);
        deltaUv1.sub(uv0);
        Vector2f deltaUv2 = new Vector2f(uv2);
        deltaUv2.sub(uv0);

        float r = 1.0f / (deltaUv1.x * deltaUv2.y - deltaUv1.y * deltaUv2.x);
        deltaPos1.mul(deltaUv2.y);
        deltaPos2.mul(deltaUv1.y);

        Vector3f tangent = new Vector3f(deltaPos1);
        tangent.sub(deltaPos2);
        tangent.mul(r);
        v0.addTangent(tangent);
        v1.addTangent(tangent);
        v2.addTangent(tangent);
    }

    /**
     * Processes a vertex and updates the indices list.
     *
     * @param vertex The vertex data.
     * @param vertices The list of vertices.
     * @param indices The list of indices.
     * @return The processed vertex.
     */
    private static Vertex processVertex(String[] vertex, List<Vertex> vertices,
                                        List<Integer> indices) {
        int index = Integer.parseInt(vertex[0]) - 1;
        Vertex currentVertex = vertices.get(index);
        int textureIndex = Integer.parseInt(vertex[1]) - 1;
        int normalIndex = Integer.parseInt(vertex[2]) - 1;
        if (!currentVertex.isSet()) {
            currentVertex.setTextureIndex(textureIndex);
            currentVertex.setNormalIndex(normalIndex);
            indices.add(index);
            return currentVertex;
        } else {
            return dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices,
                    vertices);
        }
    }

    /**
     * Converts the list of indices to an array.
     *
     * @param indices The list of indices.
     * @return The array of indices.
     */
    private static int[] convertIndicesListToArray(List<Integer> indices) {
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indicesArray.length; i++) {
            indicesArray[i] = indices.get(i);
        }
        return indicesArray;
    }

    /**
     * Converts the vertex data to arrays.
     *
     * @param vertices The list of vertices.
     * @param textures The list of texture coordinates.
     * @param normals The list of normal vectors.
     * @param verticesArray The array to store vertex positions.
     * @param texturesArray The array to store texture coordinates.
     * @param normalsArray The array to store normal vectors.
     * @param tangentsArray The array to store tangents.
     * @return The furthest point distance.
     */
    private static float convertDataToArrays(List<Vertex> vertices, List<Vector2f> textures,
                                             List<Vector3f> normals, float[] verticesArray, float[] texturesArray,
                                             float[] normalsArray, float[] tangentsArray) {
        float furthestPoint = 0;
        for (int i = 0; i < vertices.size(); i++) {
            Vertex currentVertex = vertices.get(i);
            if (currentVertex.getLength() > furthestPoint) {
                furthestPoint = currentVertex.getLength();
            }
            Vector3f position = currentVertex.getPosition();
            Vector2f textureCoord = textures.get(currentVertex.getTextureIndex());
            Vector3f normalVector = normals.get(currentVertex.getNormalIndex());
            Vector3f tangent = currentVertex.getAverageTangent();
            verticesArray[i * 3] = position.x;
            verticesArray[i * 3 + 1] = position.y;
            verticesArray[i * 3 + 2] = position.z;
            texturesArray[i * 2] = textureCoord.x;
            texturesArray[i * 2 + 1] = 1 - textureCoord.y;
            normalsArray[i * 3] = normalVector.x;
            normalsArray[i * 3 + 1] = normalVector.y;
            normalsArray[i * 3 + 2] = normalVector.z;
            tangentsArray[i * 3] = tangent.x;
            tangentsArray[i * 3 + 1] = tangent.y;
            tangentsArray[i * 3 + 2] = tangent.z;
        }
        return furthestPoint;
    }

    /**
     * Handles already processed vertices and creates duplicate vertices if necessary.
     *
     * @param previousVertex The previously processed vertex.
     * @param newTextureIndex The new texture index.
     * @param newNormalIndex The new normal index.
     * @param indices The list of indices.
     * @param vertices The list of vertices.
     * @return The processed vertex.
     */
    private static Vertex dealWithAlreadyProcessedVertex(Vertex previousVertex, int newTextureIndex,
                                                         int newNormalIndex, List<Integer> indices, List<Vertex> vertices) {
        if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
            indices.add(previousVertex.getIndex());
            return previousVertex;
        } else {
            Vertex anotherVertex = previousVertex.getDuplicateVertex();
            if (anotherVertex != null) {
                return dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex,
                        newNormalIndex, indices, vertices);
            } else {
                Vertex duplicateVertex = new Vertex(vertices.size(), previousVertex.getPosition());
                duplicateVertex.setTextureIndex(newTextureIndex);
                duplicateVertex.setNormalIndex(newNormalIndex);
                previousVertex.setDuplicateVertex(duplicateVertex);
                vertices.add(duplicateVertex);
                indices.add(duplicateVertex.getIndex());
                return duplicateVertex;
            }
        }
    }

    /**
     * Removes unused vertices and averages their tangents.
     *
     * @param vertices The list of vertices.
     */
    private static void removeUnusedVertices(List<Vertex> vertices) {
        for (Vertex vertex : vertices) {
            vertex.averageTangents();
            if (!vertex.isSet()) {
                vertex.setTextureIndex(0);
                vertex.setNormalIndex(0);
            }
        }
    }

}
